package garden.delights.earthly.imageserver.jaxrs;

import static net.aequologica.neo.geppaequo.config.ConfigRegistry.CONFIG_REGISTRY;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.Collections;
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
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.server.ManagedAsync;
import org.glassfish.jersey.server.mvc.Viewable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import garden.delights.earthly.imageserver.config.ImageServerConfig;
import garden.delights.earthly.imageserver.persistence.Persistor;
import garden.delights.earthly.imageserver.persistence.Point;
import garden.delights.earthly.imageserver.randomizer.RectangleRandomizer;
import garden.delights.earthly.imageserver.randomizer.RectangleRandomizerUtil.Rectangle;

@Singleton
@javax.ws.rs.Path("/image/v1")
public class ImagesResource {

    private final static Logger log = LoggerFactory.getLogger(ImagesResource.class);

    final private ThreadSafeBufferedImage threadSafeSource;
    final private Persistor               persistor;
    final private CacheControl            noCacheMaxAgeZeroMustRevalidateNoStore;

    public ImagesResource() {
        this.threadSafeSource = new ThreadSafeBufferedImage();
        this.persistor        = new Persistor();
        
        // Cache-Control: no-cache, max-age=0, must-revalidate, no-store
        this.noCacheMaxAgeZeroMustRevalidateNoStore = new CacheControl();
        this.noCacheMaxAgeZeroMustRevalidateNoStore.setNoCache(true);
        this.noCacheMaxAgeZeroMustRevalidateNoStore.setMaxAge(0);
        this.noCacheMaxAgeZeroMustRevalidateNoStore.setMustRevalidate(true);
        this.noCacheMaxAgeZeroMustRevalidateNoStore.setNoStore(true);
    }

    @POST
    // http://allegro.tech/2014/10/async-rest.html
    @ManagedAsync
    @javax.ws.rs.Path("/reload")
    public void reload(@Suspended final AsyncResponse                         asyncResponse,
                       @Context   final javax.servlet.http.HttpServletRequest request) throws IOException {
        this.threadSafeSource.loadImage(request, true /* forces reload */);;
        asyncResponse.resume(Boolean.TRUE);
    }
    
    @GET
    @javax.ws.rs.Path("/points")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Point>getPoints() throws SQLException, IOException {
        return persistor.get();
    }

    public class Both {
        private List<Point> points;
        private Exception   exception;
        public List<Point> getPoints() {
            return points;
        }
        public void setPoints(List<Point> points) {
            this.points = points;
        }
        public Exception getException() {
            return exception;
        }
        public void setException(Exception exception) {
            this.exception = exception;
        }
    };
    @GET
    @javax.ws.rs.Path("/points")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getPointsAsHtml() {
        Both both = new Both();
        try {
            both.setPoints(persistor.get());
            both.setException(null);
        } catch (Exception e) {
            log.warn("ignored exception {}, caused by {}", e.getMessage(), e.getCause() == null ? "" : e.getCause().getMessage());
            both.setPoints(Collections.emptyList());
            both.setException(e);
        }
        return new Viewable("/WEB-INF/image-server/index", both);
    }

    @GET
    @javax.ws.rs.Path("/points/count")
    @Produces(MediaType.TEXT_PLAIN)
    public long getPointsCount() throws SQLException, IOException  {
        return persistor.getCount();
    }
    
    @DELETE
    @javax.ws.rs.Path("/points")
    @Produces(MediaType.TEXT_PLAIN)
    public int deleteAllPoints() throws SQLException, IOException {
        return persistor.deleteAll();
    }
    
    @GET
    @javax.ws.rs.Path("/metadata")
    @Produces(MediaType.APPLICATION_JSON)
    public ThreadSafeBufferedImage.Metadata metadata(@Context javax.servlet.http.HttpServletRequest request) throws IOException {
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
        
        // only store coordinates when request comes from main front-end
        boolean storeCropCoordinates = false;
        String referer = request.getHeader("Referer");
        if (referer!=null && referer.startsWith("http://earthlydelights.garden")) {
            storeCropCoordinates = true;
        }
        
        // get sub-image (will be full image if dimensions are larger)
        final BufferedImage croppedImage = this.threadSafeSource.getCroppedImage(widthParam, heightParam, storeCropCoordinates);

        // prepare streaming
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
                }
            }
        };
        
        // go
        return Response.ok(streamOut).cacheControl(noCacheMaxAgeZeroMustRevalidateNoStore).build();
    }

    private class ThreadSafeBufferedImage {
        final private ReentrantReadWriteLock lock;
        
        private ImageServerConfig config;

        private BufferedImage source;
        private Metadata metadata;
        
        private Metadata getMetadata() {
            if (this.metadata == null) {
                throw new IllegalStateException("no image loaded");
            }
            return this.metadata;
        }
        
        public ThreadSafeBufferedImage() {
            this.lock   = new ReentrantReadWriteLock();
            this.config = CONFIG_REGISTRY.getConfig(ImageServerConfig.class);
        }

        private Metadata loadImage(HttpServletRequest request, boolean force) throws IOException {
            // if there is already something and nobody asks me to change, do nothing 
            if (this.source != null && !force) {
                return this.metadata;
            }
            
            // get url where to load image from 
            final String            urlParam = URLDecoder.decode(this.config.getImage(), "UTF8");
            if (urlParam == null || urlParam.isEmpty()) {
                throw new IllegalStateException("no image configured");
            }

            // load image
            final BufferedImage image;
            {
                InputStream inputStream = null;
                try {
                    // poor man's uri parser
                    if (urlParam.startsWith("classpath:/")) { 
                        final String path = urlParam.substring("classpath:/".length());
                        inputStream = this.getClass().getResourceAsStream(path);
                    } else { 
                        // uri must be local to server
                        // juggle with context / no context
                        final String  applicationUrl  = request.getRequestURL().toString().replace(request.getRequestURI(), request.getContextPath());
                        final URL     url             = UriBuilder.fromUri(URI.create(applicationUrl)).path(urlParam).build().toURL();
                        inputStream = url.openStream();
                    }
                    image = ImageIO.read(inputStream);
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                }
            }
            if (image == null) {
                throw new IOException("cannot read image from " + urlParam);
            }
            
            // write image to class variable
            this.lock.writeLock().lock();
            try {
                this.source = image;
                DataBuffer buff = image.getRaster().getDataBuffer();
                int bytes = buff.getSize() * DataBuffer.getDataTypeSize(buff.getDataType()) / 8;                
                this.metadata = new Metadata(
                        this.source.getWidth(), 
                        this.source.getHeight(),
                        bytes);
                return this.metadata;
            } finally {
                this.lock.writeLock().unlock();
            }
        }

        private void lazyLoadImage(HttpServletRequest request) throws IOException {
            loadImage(request, false);
        }
        
        private BufferedImage getCroppedImage(final int widthParam, final int heightParam, boolean storeCoordinates) throws IOException {
            BufferedImage ret;
            this.lock.readLock().lock();
            try {
                ret = this.source;
            } finally {
                this.lock.readLock().unlock();
            }
            if (widthParam < ret.getWidth() && heightParam < ret.getHeight()) {
                
                
                final Rectangle<Long> rectangle = getCropRectangle(ret.getWidth(), ret.getHeight(), widthParam, heightParam, this.config.getRandomizerType());
                
                ret = this.source.getSubimage( 
                        rectangle.x.intValue(), 
                        rectangle.y.intValue(), 
                        rectangle.w.intValue(), 
                        rectangle.h.intValue());
                
                if (storeCoordinates) {
                    // write to DB in a thread
                    new Thread(() -> { 
                        try {
                            ImagesResource.this.persistor.store(rectangle.x.longValue(), rectangle.y.longValue());
                        } catch (Exception e) {
                            log.error(e.getMessage());
                        }
                    }).start();
                }
            }
            return ret;
        }
        
        @SuppressWarnings("unused")
        @JsonIgnoreProperties
        public class Metadata {

            @JsonProperty
            private long width;
            @JsonProperty
            private long height;
            @JsonProperty
            private int bytes;
            
            public Metadata() {
                super();
            }

            public Metadata(long width, long height, int bytes) {
                super();
                this.width = width;
                this.height = height;
                this.bytes = bytes;
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

            public int getBytes() {
                return bytes;
            }

            public void setBytes(int bytes) {
                this.bytes = bytes;
            }

            @JsonProperty
            public String getTitle() {
                return ThreadSafeBufferedImage.this.config.getTitle();
            }

            @JsonProperty
            public String getImage() {
                return ThreadSafeBufferedImage.this.config.getImage();
            }

            @JsonProperty
            public String getApp() {
                return ThreadSafeBufferedImage.this.config.getApp();
            }

            @JsonProperty
            public String getWikipedia() {
                return ThreadSafeBufferedImage.this.config.getWikipedia();
            }
            
        }
    }
    
    
    static private Rectangle<Long> getCropRectangle(
                final int sourceWidth, 
                final int sourceHeight, 
                final int targetWidth, 
                final int targetHeight,
                final RectangleRandomizer.Type type) throws IOException {
        int grid = 20;
        long W = sourceWidth/grid; 
        long H = sourceHeight/grid; 
        long w = targetWidth/grid;
        long h = targetHeight/grid; 
        final RectangleRandomizer randomizer  = new RectangleRandomizer(
                W, 
                H, 
                w,
                h, 
                type);
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
