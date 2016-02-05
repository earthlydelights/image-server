package garden.delights.earthly.jaxrs;

import static garden.delights.earthly.randomizer.RectangleRandomizer.Type.UNIFORM;
import static net.aequologica.neo.geppaequo.config.ConfigRegistry.getConfig;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.server.mvc.Viewable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import garden.delights.earthly.model.Point;
import garden.delights.earthly.persistence.Persistor;
import garden.delights.earthly.randomizer.RectangleRandomizer;
import garden.delights.earthly.randomizer.RectangleRandomizerUtil.Rectangle;
import net.aequologica.neo.imageserver.config.ImageServerConfig;

@Singleton
@javax.ws.rs.Path("/image/v1")
public class ImagesResource {

    private final static Logger log = LoggerFactory.getLogger(ImagesResource.class);

    final ThreadSafeBufferedImage threadSafeSource;
    final Persistor               persistor;
    
    public ImagesResource() {
        this.threadSafeSource = new ThreadSafeBufferedImage();
        this.persistor        = new Persistor();
    }

    @GET
    @javax.ws.rs.Path("/reload")
    @Produces(MediaType.TEXT_PLAIN)
    public Response reload(@Context javax.servlet.http.HttpServletRequest request) throws IOException {
        this.threadSafeSource.loadImage(request, true /* forces reload */);
        return Response.ok(Status.NO_CONTENT).build();
    }
    
    @GET
    @javax.ws.rs.Path("/points")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Point>getPoints() throws SQLException, IOException {
        return persistor.get();
    }

    @GET
    @javax.ws.rs.Path("/points")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getPointsAsHtml() throws SQLException, IOException  {
        List<Point> list = persistor.get();
        return new Viewable("/WEB-INF/image-server/index", list);
    }

    @GET
    @javax.ws.rs.Path("/points/count")
    @Produces(MediaType.TEXT_PLAIN)
    public long getPointsCount() throws SQLException, IOException  {
        return persistor.getCount();
    }
    
    @GET
    @javax.ws.rs.Path("/metadata")
    @Produces(MediaType.APPLICATION_JSON)
    public Metadata metadata(@Context javax.servlet.http.HttpServletRequest request) throws IOException {
        this.threadSafeSource.lazyLoadImage(request);
        return this.threadSafeSource.getMetadata();
    }

    @GET
    @javax.ws.rs.Path("/crop")
    @Produces("image/jpeg")
    public Response crop(
            @DefaultValue("1920") @QueryParam("width")   final int  widthParam,
            @DefaultValue("1080") @QueryParam("height")  final int  heightParam,
            @DefaultValue("50")   @QueryParam("quality") final int  qualityParam,
            @Context javax.servlet.http.HttpServletRequest request
      ) throws IOException {

        this.threadSafeSource.lazyLoadImage(request);
        
        // coerce quality from any integer to a float between 0 and 1
        final float quality; 
        if (qualityParam <= 0) {
            quality = 0f;
        } else if (qualityParam > 100f) {
            quality = 1f;
        } else {
            quality = (float)qualityParam/100f;
        }
        
        final BufferedImage croppedImage = this.threadSafeSource.getCroppedImage(widthParam, heightParam);

        StreamingOutput streamOut = new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                
                try (ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(os)) {
                    // cf. http://www.universalwebservices.net/web-programming-resources/java/adjust-jpeg-image-compression-quality-when-saving-images-in-java

                    final ImageWriter       jpegImageWriter = ImageIO.getImageWritersByFormatName("jpeg").next();
                    final ImageWriteParam   imageWriteParam = jpegImageWriter.getDefaultWriteParam();
                    final IIOMetadata       imageMetadata   = jpegImageWriter.getDefaultImageMetadata(ImageTypeSpecifier.createFromBufferedImageType(croppedImage.getType()), imageWriteParam);
                    final IIOImage          image           = new IIOImage(croppedImage, null, imageMetadata);
                    
                    imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    imageWriteParam.setCompressionQuality(quality);
                    jpegImageWriter.setOutput(imageOutputStream);
                    jpegImageWriter.write(null, image, imageWriteParam );
                    jpegImageWriter.dispose();
                    
                } catch (Exception e) {
                    log.error("ignored exception in StreamingOutput.write mehod" , e);
                } finally {
                }
            }
        };
        // Cache-Control: no-cache, max-age=0, must-revalidate, no-store
        CacheControl control = new CacheControl();
        control.setNoCache(true);
        control.setMaxAge(0);
        control.setMustRevalidate(true);
        control.setNoStore(true);
        return Response.ok(streamOut).cacheControl(control).build();
    }

    private class ThreadSafeBufferedImage {
        final private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        
        private BufferedImage source;
        private Metadata metadata;
        
        private Metadata getMetadata() {
            if (this.metadata == null) {
                throw new IllegalStateException("no image loaded");
            }
            return this.metadata;
        }
        
        private void loadImage(HttpServletRequest request, boolean force) throws IOException {
            if (this.source != null && !force) {
                return;
            }
            
            this.lock.writeLock().lock();
            try {
                ImageServerConfig config   = getConfig(ImageServerConfig.class);
                String            urlParam = config.getImage();
                if (urlParam == null) {
                    throw new IllegalStateException("no image configured");
                }
                String  applicationUrl  = request.getRequestURL().toString().replace(request.getRequestURI(), request.getContextPath());
                URL     url             = UriBuilder.fromUri(URI.create(applicationUrl)).path(urlParam).build().toURL();

                this.source = ImageIO.read(url);
                
                this.metadata = new Metadata(
                        this.source.getWidth(), 
                        this.source.getHeight(),
                        config.getTitle(), 
                        config.getImage(), 
                        config.getWikipedia());

            } finally {
                this.lock.writeLock().unlock();
            }       
        }

        private void lazyLoadImage(HttpServletRequest request) throws IOException {
            loadImage(request, false);
        }
        
        private BufferedImage getCroppedImage(final int widthParam, final int heightParam) throws IOException {
            BufferedImage ret;
            this.lock.readLock().lock();
            try {
                ret = this.source;
            } finally {
                this.lock.readLock().unlock();
            }
            if (widthParam < ret.getWidth() && heightParam < ret.getHeight()) {
                final Rectangle<Long> rectangle = getCropRectangle(ret.getWidth(), ret.getHeight(), widthParam, heightParam);
                
                ret = this.source.getSubimage( 
                        rectangle.x.intValue(), 
                        rectangle.y.intValue(), 
                        rectangle.w.intValue(), 
                        rectangle.h.intValue());
                
                Runnable save2db = () -> { 
                    try (final Persistor p = new Persistor()) {
                        p.store(rectangle.x.longValue(), rectangle.y.longValue());
                    } catch (Exception e) {
                        log.error(e.getMessage());
                    }
                };
                 
                // start the thread
                new Thread(save2db).start();
            }
            return ret;
        }
    }
    
    @JsonIgnoreProperties
    public static class Metadata {

        @JsonProperty
        private long width;
        @JsonProperty
        private long height;
        @JsonProperty
        private String title;
        @JsonProperty
        private String wikipedia;
        @JsonProperty
        private String image;
        
        public Metadata() {
            super();
        }

        public Metadata(long width, long height, String title, String image, String wikipedia) {
            super();
            this.width = width;
            this.height = height;
            this.title = title;
            this.image = image;
            this.wikipedia = wikipedia;
        }

        public long getWidth() {
            return width;
        }
        
        public void setWidth(long width) {
            this.width = width;
        }
        
        public long getHeight() {
            return height;
        }
        
        public void setHeight(long height) {
            this.height = height;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getWikipedia() {
            return wikipedia;
        }

        public void setWikipedia(String wikipedia) {
            this.wikipedia = wikipedia;
        }
        
    }
    
    static private Rectangle<Long> getCropRectangle(final int sourceWidth, final int sourceHeight, final int targetWidth, final int targetHeight) throws IOException {
        int grid = 20;
        long W = sourceWidth/grid; 
        long H = sourceHeight/grid; 
        long w = targetWidth/grid;
        long h = targetHeight/grid; 
        final RectangleRandomizer   randomizer  = new RectangleRandomizer(
                W, 
                H, 
                w,
                h, 
                UNIFORM);
        final Rectangle<Long> random = randomizer.getRandomRectangle();
        long x = random.x.longValue()*grid;
        long y = random.y.longValue()*grid;
        long width = (long)Math.floor(Math.min(random.w.longValue()*grid, targetWidth));
        long height = (long)Math.floor(Math.min(random.h.longValue()*grid, targetHeight));
        final Rectangle<Long> rectangle = new Rectangle<Long>(
                x,
                y,
                width,
                height, 
                a->(long)a);
        
        final boolean tooWide = ( x + width  > sourceWidth  );
        final boolean tooHigh = ( y + height > sourceHeight );
        
        log.info("\n"+
                "    {}.{} | {}x{} [source]\n"+
                "    {}.{} + {} {} [crop]\n"+
                "    {}x{} = {}.{} [bottom right corner of crop]\n"+
                "    crop too wide ? {} !\n"+
                "    crop too high ? {} !\n", 
                String.format("%04d", 0), 
                String.format("%04d", 0), 
                String.format("%04d", sourceWidth), 
                String.format("%04d", sourceHeight), 
                String.format("%04d", x), 
                String.format("%04d", y),
                "    ",
                "    ",
                String.format("%04d", width), 
                String.format("%04d", height), 
                String.format("%04d", x + width), 
                String.format("%04d", y + height), 
                tooWide ? "yes" : "no", 
                tooHigh ? "yes" : "no");

        if (tooWide || tooHigh) {
            throw new IndexOutOfBoundsException();
        }

        return rectangle;
    }

}
