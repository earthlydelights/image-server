package garden.delights.earthly.randomizer;

import static org.junit.Assert.*;

import org.junit.Test;

import garden.delights.earthly.randomizer.RectangleRandomizerUtil.Point;

public class PointTest {

    @Test
    public void test() {
        Point<Integer> i = new Point<>(3,4, a->(int)a);
        assertEquals(5, (int)i.hypothenuse());

        Point<Double> d = new Point<>(.999999999,.999999999, a->(double)a);
        assertEquals("", Math.pow(2, .5), (double)d.hypothenuse(), 0.00000001);

        Point<Float> f = new Point<>(.1f,.1f, a->(float)a);
        assertEquals(0.14142136f, (float)f.hypothenuse(), 0.);
    }

}
