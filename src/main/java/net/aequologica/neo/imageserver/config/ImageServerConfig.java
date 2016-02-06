package net.aequologica.neo.imageserver.config;

import java.io.IOException;
import java.util.Arrays;

import org.weakref.jmx.Managed;

import garden.delights.earthly.randomizer.RectangleRandomizer;
import garden.delights.earthly.randomizer.RectangleRandomizer.Type;
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

    @Managed
    public String getRandomizer() {
        return get("randomizer");
    }

    public RectangleRandomizer.Type getRandomizerType() {
        String randomizerAsString = get("randomizer");
        final Type randomizer = validateRandomizer(randomizerAsString);
        return randomizer;
    }

    @Managed
    public void setRandomizer(String randomizerAsString) {
        final Type randomizer = validateRandomizer(randomizerAsString);
        set("randomizer", randomizer.toString());
    }

    private Type validateRandomizer(String randomizerAsString) {
        final Type randomizer;
        try {
            randomizer = RectangleRandomizer.Type.valueOf(randomizerAsString.toUpperCase());
            if (randomizer == null) {
                throw new NullPointerException();
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "\"" + randomizerAsString + "\"" + 
                    " is not a valid randomizer. Valid values are " + 
                    Arrays.toString(RectangleRandomizer.Type.values()), e);
        }
        return randomizer;
    }

    
}
