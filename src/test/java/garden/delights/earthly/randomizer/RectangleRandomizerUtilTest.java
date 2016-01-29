package garden.delights.earthly.randomizer;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.ThreadLocalRandom;

import org.junit.Test;

import garden.delights.earthly.randomizer.RectangleRandomizer;
import garden.delights.earthly.randomizer.Computer.Store;
import garden.delights.earthly.randomizer.Computer.WeightIntegraleAndSegment;
import garden.delights.earthly.randomizer.RectangleRandomizer.UniformRectangleRandomizer;
import garden.delights.earthly.randomizer.RectangleRandomizerUtil.Dimension;
import garden.delights.earthly.randomizer.RectangleRandomizerUtil.Point;

public class RectangleRandomizerUtilTest {

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
        /*
        System.out.println( "^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^");
        System.out.println( "  "+uniform.getClass().getSimpleName() );
        System.out.println( "  "+"D " + Dw + "x" + Dh );
        System.out.println( "  "+"d " + dw + "x" + dh );
        System.out.println( "  "+"size " + uniform.getSize()    );
        System.out.println( "v v v v v v v v v v v");
        */
        double                      size         = uniform.getSize();
        double                      weightedSize = uniform.getWeightedSize();
        WeightIntegraleAndSegment[]  offsets      = store.getOffsetsJustForTesting();

        double[] values = new double[12];
        values[0] = 0.;
        values[1] = weightedSize;
        values[2] = weightedSize + 1.;
        for (int i = 3; i<values.length; i++) {
            values[i] = ThreadLocalRandom.current().nextDouble()*(weightedSize);;
        }
        for (double search : values) {
            // map uses dichotomy search in the background
            double actual = uniform.map(search); 
            
            // verify linear search is equal to dichotomical search
            boolean found = false;
            for (int expected=0; expected<offsets.length-1; expected++) {

                if (offsets[expected].segment <= search && search < offsets[expected+1].segment) {
                    assertEquals(""+offsets[expected]+" <= "+search +" < "+offsets[expected+1], expected, actual, 0.);
                    found = true;
                    break;
                }
            }
            if (!found) {
                assertEquals("when not found, uniform.map returns the upper limit", size, actual, 0.);
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
