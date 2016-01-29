package garden.delights.earthly.jaxrs;

import static garden.delights.earthly.randomizer.RectangleRandomizer.Type.UNIFORM;

import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;
import javax.inject.Singleton;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import garden.delights.earthly.randomizer.RectangleRandomizer;
import garden.delights.earthly.randomizer.RectangleRandomizerUtil.Rectangle;

@Singleton
@javax.ws.rs.Path("/image/v1")
public class ImagesResource {
    
    PrintStream sysout = null;
    
    public ImagesResource() throws Exception {
        if (true) {
            final Path path = Files.createTempFile("geppaequo-images-resource-test", ".txt");
            System.out.println("Temp file : " + path);
            // path.toFile().deleteOnExit();

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        // Files.delete(path);
                        // System.out.println("deleted file at " + path);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            final OutputStream fos = new FileOutputStream(path.toFile());
            this.sysout = new PrintStream(fos);
            this.sysout.println(path.toAbsolutePath().toString());
            this.sysout.flush();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(ImagesResource.class);

    private BufferedImage source;

    @GET
    @javax.ws.rs.Path("/crop")
    @Produces("image/jpeg")
    public Response crop(
            // @DefaultValue("/geppaequo-api/stnemucod/v1/document/images/bosch-the-garden-of-earthly-delights.jpg") @QueryParam("url") String urlParam,
            @DefaultValue("1920") @QueryParam("width")   final int  widthParam,
            @DefaultValue("1080") @QueryParam("height")  final int  heightParam,
            @DefaultValue("50")   @QueryParam("quality") final int  qualityParam,
            @Context javax.servlet.http.HttpServletRequest request
      ) throws IOException {

        // force source image
        String urlParam = "/geppaequo-api/stnemucod/v1/document/images/bosch-the-garden-of-earthly-delights.jpg";

        // coerce quality from any integer to a float between 0 and 1
        final float quality; 
        if (qualityParam <= 0) {
            quality = 0f;
        } else if (qualityParam > 100f) {
            quality = 1f;
        } else {
            quality = (float)qualityParam/100f;
        }
        
        // get source URL
        final URL url;
        if (urlParam.startsWith("http")) {
            url = URI.create(urlParam).toURL();
        } else if (urlParam.startsWith("/"))  {
            String applicationUrl = request.getRequestURL().toString().replace(request.getRequestURI(), request.getContextPath());
            url = UriBuilder.fromUri(URI.create(applicationUrl)).path(urlParam).build().toURL();
        } else {
            url = null;
        }
        
        if (url == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        if (source == null) {
            source = ImageIO.read(url);
            if (this.sysout != null) {
                this.sysout.println(String.format("x\t%4d\ty\t%4d", source.getWidth(), source.getHeight()));
                this.sysout.println("-----------------------------------------");
                this.sysout.flush();
            }
        }
        
        Rectangle<Long> rectangle = getCropRectangle(source.getWidth(), source.getHeight(), widthParam, heightParam);
        
        final BufferedImage croppedImage = source.getSubimage( 
                rectangle.x.intValue(), 
                rectangle.y.intValue(), 
                rectangle.w.intValue(), 
                rectangle.h.intValue());

        StreamingOutput streamOut = new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                
                // log.info("starting streaming");
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
                    // log.info("streaming finished");
                }
            }
        };
        return Response.ok(streamOut).build();
    }

    Rectangle<Long> getCropRectangle(final int sourceWidth, final int sourceHeight, final int targetWidth, final int targetHeight) throws IOException {
        int grid = 1;
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
        
        if (this.sysout != null) {
            this.sysout.println(String.format("x\t%4d\ty\t%4d", x, y));
            this.sysout.flush();
        }

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
