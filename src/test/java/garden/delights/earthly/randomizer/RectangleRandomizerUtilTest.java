package garden.delights.earthly.randomizer;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Test;

import garden.delights.earthly.randomizer.Computer.Store;
import garden.delights.earthly.randomizer.RectangleRandomizer.UniformRectangleRandomizer;
import garden.delights.earthly.randomizer.RectangleRandomizerUtil.Dimension;
import garden.delights.earthly.randomizer.RectangleRandomizerUtil.Point;

public class RectangleRandomizerUtilTest {

    @Test
    public void testType() {
        System.out.println(Arrays.toString(RectangleRandomizer.Type.values()));
    }
    @Test
    public void testDichotomy() {
        @SuppressWarnings("unused")
        int Dw, Dh, dw, dh;
        RectangleRandomizer randomizer = new RectangleRandomizer(
                Dw=8, Dh=7, 
                dw=5, dh=2,
                RectangleRandomizer.Type.UNIFORM);
        final UniformRectangleRandomizer uniform = (UniformRectangleRandomizer)randomizer.internal;
        final Store                      store   = (Store)randomizer.computer.store;

        store.lazyLoad();

        double size     = uniform.getSize();
        double[] offsets  = store.unauthorizedGetSegmentsJustForTesting();

        double[] values = new double[12];
        values[0] = 0.;
        values[1] = 1.;
        values[2] = 2.;
        for (int i = 3; i<values.length; i++) {
            values[i] = ThreadLocalRandom.current().nextDouble();
        }
        double[] integs = store.unauthorizedGetSegmentsJustForTesting();
        for (double search : values) {
            System.out.println("-----------");
            // map uses dichotomy search in the background
            double actual = uniform.map(search); 
            
            // verify linear search is equal to dichotomical search
            boolean found = false;
            for (int expected=0; expected<offsets.length-1; expected++) {
                double low = integs[expected];
                double high = integs[expected+1];
                if (low <= search && search < high) {
                    assertEquals(""+low+" <= "+search +" < "+high, expected, actual, 0.);
                    System.out.println("found also " + low + " <= " +  search + " <= " + high);
                    found = true;
                    break;
                }

            }
            if (!found) {
                System.out.println("when not found "+search+", uniform.map returns the upper limit " + actual);
                assertEquals("["+search+"] when not found, uniform.map returns the upper limit", size, actual, 0.);
            }
        }

    }

    @Test
    public void testIntegerPointIndexCodec() {
        Dimension<Integer> dim = new Dimension<>(3, 2, a->(int)a );
        for (Integer i=0; i<dim.getSize(); i++) {
            Point<Integer> p = dim.pointFromIndex(i);
            Integer i2 = dim.indexFromPoint(p);
            assertEquals(i, i2, 0.);
        }
    }
    

    @Test
    public void testDoublePointIndexCodec() {
        Dimension<Double> dim = new Dimension<>(3., 2., a->(double)a);
        for (double i=0; i<dim.getSize(); i++) {
            Point<Double> p = dim.pointFromIndex(i);
            Double i2 = dim.indexFromPoint(p);
            assertEquals(i, i2, 0.);
        }
    }
    
}
