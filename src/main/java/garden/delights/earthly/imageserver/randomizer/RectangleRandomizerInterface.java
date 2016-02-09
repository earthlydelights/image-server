package garden.delights.earthly.imageserver.randomizer;

import garden.delights.earthly.imageserver.randomizer.RectangleRandomizerUtil.Rectangle;

@FunctionalInterface
public interface RectangleRandomizerInterface {
    Rectangle<Long> getRandomRectangle();
}

