package net.aequologica.neo.imageserver.config;

import java.io.IOException;

import org.weakref.jmx.Managed;

import net.aequologica.neo.geppaequo.config.AbstractConfig;
import net.aequologica.neo.geppaequo.config.Config;

@Config(name = "imageserver")
public final class ImageServerConfig extends AbstractConfig {

    public ImageServerConfig() throws IOException {
        super();
    }
    
    @Managed
    public String getImage() {
        return get("image");
    }

    @Managed
    public void setImage(String image) {
        set("image", image);
    }

    @Managed
    public String getTitle() {
        return get("title");
    }

    @Managed
    public void setTitle(String title) {
        set("title", title);
    }

    @Managed
    public String getWikipedia() {
        return get("wikipedia");
    }

    @Managed
    public void setWikipedia(String wikipedia) {
        set("wikipedia", wikipedia);
    }

    
}