package garden.delights.earthly.randomizer;

import garden.delights.earthly.randomizer.RectangleRandomizerUtil.Rectangle;

@FunctionalInterface
public interface RectangleRandomizerInterface {
    Rectangle<Long> getRandomRectangle();
}

