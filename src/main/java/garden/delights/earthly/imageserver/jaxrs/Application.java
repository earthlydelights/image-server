package garden.delights.earthly.imageserver.jaxrs;

import static net.aequologica.neo.geppaequo.config.ConfigRegistry.scanConfigs;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.jsp.JspMvcFeature;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import net.aequologica.neo.geppaequo.config.ConfigRegistry;

@ApplicationPath("earthly-delights-garden-api")
public class Application extends ResourceConfig {

    public Application() {

        scanConfigs(new String[] {"net.aequologica.neo.imageserver.config"});
        
        register(LoggingFilter.class);
        register(JspMvcFeature.class);
        register(MultiPartFeature.class);
        register(JacksonJsonProvider.class);

        register(ImagesResource.class);
        
        
    }

}
