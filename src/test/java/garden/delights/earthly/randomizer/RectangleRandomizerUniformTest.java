package garden.delights.earthly.randomizer;

import static org.junit.Assert.assertEquals;

import java.text.NumberFormat;

import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.junit.Test;

import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;

import garden.delights.earthly.randomizer.Computer;
import garden.delights.earthly.randomizer.RectangleRandomizer;
import garden.delights.earthly.randomizer.RectangleRandomizerInterface;
import garden.delights.earthly.randomizer.Computer.Store;
import garden.delights.earthly.randomizer.Computer.WeightIntegraleAndSegment;
import garden.delights.earthly.randomizer.RectangleRandomizer.AbstractRectangleRandomizer;
import garden.delights.earthly.randomizer.RectangleRandomizer.UniformRectangleRandomizer;
import garden.delights.earthly.randomizer.RectangleRandomizerUtil.Dimension;
import garden.delights.earthly.randomizer.RectangleRandomizerUtil.Point;
import garden.delights.earthly.randomizer.RectangleRandomizerUtil.Rectangle;

public class RectangleRandomizerUniformTest {

    static NumberFormat percentFormat = NumberFormat.getPercentInstance();
    static NumberFormat percentFormat2 = NumberFormat.getPercentInstance();
    static {
        percentFormat.setMaximumFractionDigits(2);
        percentFormat.setMinimumFractionDigits(2);
        percentFormat.setMinimumIntegerDigits(2);

        percentFormat2.setMaximumFractionDigits(4);
        percentFormat2.setMinimumFractionDigits(4);
        percentFormat2.setMinimumIntegerDigits(2);
    }
    
    @Test
    public void test5x2() {
        Dimension<Integer> D = new Dimension<>(5, 5, a->(int)a );
        Dimension<Integer> d = new Dimension<>(2, 2, a->(int)a );
        int[] weights = new int[] {
           9, 12, 12, 9,
          12, 16, 16, 12,
          12, 16, 16, 12,
           9, 12, 12, 9,
        };
        /*
        multiply big Dimension(5x5) 
        
            x * y |     0       1       2       3       4
            ──────┼─────────────────────────────────────────────
                0 │     1       2       3       4       5
                1 │     2       4       6       8      10
                2 │     3       6       9      12      15
                3 │     4       8      12      16      20
                4 │     5      10      15      20      25
        
        mirror small Dimension(2x2) 
        
             frqs |     0       1       2       3       4
            ──────┼─────────────────────────────────────────────
                0 │     1       2       2       2       1
                1 │     2       4       4       4       2
                2 │     2       4       4       4       2
                3 │     2       4       4       4       2
                4 │     1       2       2       2       1
        
        weightOfCrop small Dimension(2x2) 
        
            integ |     0       1       2       3
            ──────┼────────────────────────────────────
                0 │     9      12      12       9
                1 │    12      16      16      12
                2 │    12      16      16      12
                3 │     9      12      12       9
        
        */
        
        theTest(D,d, weights);
    }

    @Test
    public void test5x5() {
        Dimension<Integer> D = new Dimension<>(5, 5, a->(int)a );
        Dimension<Integer> d = new Dimension<>(2, 5, a->(int)a );
        
        theTest(D,d, null);
    }

    @Test
    public void test7x5() {
        Dimension<Integer> D = new Dimension<>(7, 7, a->(int)a );
        Dimension<Integer> d = new Dimension<>(5, 5, a->(int)a );
        
/*
multiply big Dimension(7x7) 

    x * y |     0       1       2       3       4       5       6
    ──────┼───────────────────────────────────────────────────────────────
        0 │     1       2       3       4       5       6       7
        1 │     2       4       6       8      10      12      14
        2 │     3       6       9      12      15      18      21
        3 │     4       8      12      16      20      24      28
        4 │     5      10      15      20      25      30      35
        5 │     6      12      18      24      30      36      42
        6 │     7      14      21      28      35      42      49

mirror small Dimension(5x5) 

     frqs |     0       1       2       3       4       5       6
    ──────┼───────────────────────────────────────────────────────────────
        0 │     1       2       2       2       2       2       1
        1 │     2       4       4       4       4       4       2
        2 │     2       4       4       4       4       4       2
        3 │     2       4       4       4       4       4       2
        4 │     2       4       4       4       4       4       2
        5 │     2       4       4       4       4       4       2
        6 │     1       2       2       2       2       2       1

weightOfCrop small Dimension(5x5) 

    integ |     0       1       2
    ──────┼───────────────────────────
        0 │    81      90      81
        1 │    90     100      90
        2 │    81      90      81

*/        
        int[] weights = new int[] {
                81,   90,   81,
                90,  100,   90,
                81,   90,   81,                
        };
                
        theTest(D,d, weights);
    }

    @Test
    public void test8x5x5x2() {
        Dimension<Integer> D = new Dimension<>(8, 5, a->(int)a );
        Dimension<Integer> d = new Dimension<>(5, 2, a->(int)a );
        int[] weights = new int[] {
                36,   42,   42,   36,
                48,   56,   56,   48,
                48,   56,   56,   48,
                36,   42,   42,   36,
        };
                
        theTest(D,d, weights);
    }

    public static void theTest(Dimension<Integer> D, Dimension<Integer> d, int[] weights) {
        RectangleRandomizer randomizer = new RectangleRandomizer(
                D.w,
                D.h, 
                d.w,
                d.h, 
                RectangleRandomizer.Type.UNIFORM);

        final RectangleRandomizerInterface  rri         = randomizer.internal;
        final AbstractRectangleRandomizer   ari         = (AbstractRectangleRandomizer)rri;
        final Computer computer                         = randomizer.computer;
        final UniformRectangleRandomizer    uniform     = (rri instanceof UniformRectangleRandomizer ? (UniformRectangleRandomizer)rri : null);
        final Store                         store       = (Store)computer.store;

        final long draws = 10000000L;
        final double acceptable_variance_delta = .1;

        Multiset<Long> set = TreeMultiset.create(
            (x, y) -> Long.compare(x, y)
        );
        
        final long size = ari.getSize();
        final long totalWeight = computer.getTotalWeight();
        final double weightedSize = uniform.getWeightedSize();

        System.out.println( "^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^");
        System.out.println( "  "+rri.getClass().getSimpleName() );
        System.out.println( "  "+"D " + D.w + "x" + D.h );
        System.out.println( "  "+"d " + d.w + "x" + d.h );
        System.out.println( "  "+"computer big " + computer.big );
        System.out.println( "  "+"computer small " + computer.small );
        System.out.println( "  "+"computer smallest " + computer.smallest );
        System.out.println( "  "+"computer biggest " + computer.biggest );
        System.out.println( "  "+"size " + size    );
        System.out.println( "  "+"total weigth " + totalWeight    );
        System.out.println( "  "+"weighted size " + weightedSize    );
        System.out.println( "v v v v v v v v v v v");

        for (int f = 0; f<draws; f++) {
            Rectangle<Long> random = randomizer.getRandomRectangle();
            long index = ari.indexFromPoint(random.p);
            // System.out.println(index + " " + random.p);
            set.add(index);
        }
        
        double  verif_draws = 0; 
        long    verif_weights = 0; 
        
        Variance variance = new Variance();
        Variance variance_perceived = new Variance();
        /*
        {
            WeightAndSegment[] offsets = store.getOffsetsJustForTesting();
            long prev = 0;
            for (int i=0; i<offsets.length ; i++) {
                System.out.print(offsets[i].weight);
                System.out.print(" ");
                System.out.print(offsets[i].weight - prev);
                prev = offsets[i].weight;
                System.out.print(" ");
                System.out.println(offsets[i].segment);
            }
        }
        */
        
        for (long i= 0 ; i < size; i++) {
            Point<Long> p        = computer.biggest.pointFromIndex(i);
            int         count    = set.count(i);
            double      percent  = (double)count/draws;
            
            WeightIntegraleAndSegment wis     = store.get((int)i);
            WeightIntegraleAndSegment nextWis = store.get((int)i+1);
            long        weight          = wis.weight;
            @SuppressWarnings("unused")
            long        nextWeight      = nextWis.weight;
            long        integrale       = wis.integrale;
            long        nextIntegrale   = nextWis.integrale;
            double      segment         = wis.segment;
            double      nextSegment     = nextWis.segment;
            
            double      perceivedWeight = (double)weight*percent*size;
            
            variance.increment(percent);
            variance_perceived.increment(perceivedWeight);
            
            if (count!= 0 && weights != null && i < weights.length) {
                assertEquals("index "+i, weights[(int)i], weight, 0.);
            }
            
            verif_draws += percent;
            verif_weights += weight;

            System.out.print(String.format("%2d -> %s  | %s %2d (%2d + %2d = %2d) %s (%s + %s = %s) |",
                    i, 
                    percentFormat.format(percent),
                    p,
                    (int)weight,
                    (int)integrale,
                    (int)nextIntegrale-(int)integrale, 
                    (int)nextIntegrale,
                    percentFormat2.format((double)weight/totalWeight), 
                    percentFormat2.format(segment),
                    percentFormat2.format(nextSegment-segment),
                    percentFormat2.format(nextSegment)));
            System.out.print(String.format("%2.1f", perceivedWeight));
            System.out.println();
        }
        System.out.println(String.format("sum of %% draws is %s", percentFormat.format(verif_draws)));
        System.out.println(String.format("total weight is %4d", totalWeight));
        System.out.println(String.format("sum of weights is %4d", verif_weights));
        System.out.println(String.format("sum of %% weights is %s", percentFormat.format((double)verif_weights/totalWeight)));
        System.out.println(String.format("variance %f <? %f", variance.getResult(), acceptable_variance_delta));
        System.out.println(String.format("variance adjusted %f <? %f", variance_perceived.getResult(), acceptable_variance_delta));

        if (weights != null && weights.length > 0) {
            assertEquals("acceptable variance delta is "+acceptable_variance_delta, 0., variance_perceived.getResult(), acceptable_variance_delta);
        }
    }

    public static void main(String [ ] args) {
        Dimension<Integer> D = new Dimension<>(5, 5, a->(int)a );
        Dimension<Integer> d = new Dimension<>(3, 3, a->(int)a );
        RectangleRandomizerUniformTest.theTest(D,d, null);
    }
    

}
