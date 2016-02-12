package garden.delights.earthly.imageserver.jaxrs;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.jsp.JspMvcFeature;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

@ApplicationPath("earthly-delights-garden-api")
public class Application extends ResourceConfig {

    public Application() {

        register(LoggingFilter.class);
        register(JspMvcFeature.class);
        register(MultiPartFeature.class);
        register(JacksonJsonProvider.class);

        register(ImagesResource.class);
        
        
    }

}
