package garden.delights.earthly.imageserver.config;

import static net.aequologica.neo.geppaequo.config.ConfigRegistry.scanConfigs;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebListener(value="ImageServerListener")
public final class Listener implements ServletContextListener {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(Listener.class);

    @Override
    public void contextInitialized(final ServletContextEvent servletContextEvent) {
        scanConfigs(new String[] {"garden.delights.earthly.imageserver.config"});
    }

    @Override
    public void contextDestroyed(final ServletContextEvent servletContextEvent) {
    }

}
