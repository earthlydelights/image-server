package garden.delights.earthly.imageserver.randomizer;

import org.junit.Assert;
import org.junit.Test;

import garden.delights.earthly.imageserver.randomizer.RectangleRandomizer;
import garden.delights.earthly.imageserver.randomizer.Computer.Store;
import garden.delights.earthly.imageserver.randomizer.RectangleRandomizerUtil.Dimension;

public class ComputerStoreTest {

    @Test
    public void test00() {
        Dimension<Integer> d = new Dimension<>(0, 0, a->(int)a );
        double[] offsets = load(d,d);
        Assert.assertEquals(" ", 1, offsets.length);
        Assert.assertEquals("at index 0", 1., offsets[0], 0.);
    }

    @Test
    public void test11() {
        Dimension<Integer> d = new Dimension<>(1, 1, a->(int)a );
        double[] offsets = load(d,d);
        Assert.assertEquals(" ", 2, offsets.length);
        Assert.assertEquals("at index 0", 1, offsets[0], 0.);
        Assert.assertEquals("at index 1", 2, offsets[1], 0.);
    }
    
    @Test
    public void test21() {
        Dimension<Integer> d = new Dimension<>(2, 1, a->(int)a );
        Dimension<Integer> cro = new Dimension<>(1, 1, a->(int)a );
        double[] offsets = load(d,cro);

        Assert.assertEquals(" ", 3, offsets.length);
        /*
        index
                0    1   
            ╔════╤════╗  
           0║   0│   0║  
            ╚════╧════╝  
                         
         store           
                0    1   
            ╔════╤════╗  
           0║   0│   2║  
            ╚════╧════╝  
            */        
        double[] expectedOffsets = new double[] {
                1., 2., 4.
        };
        for (int i = 0; i < expectedOffsets.length; i++) {
            Assert.assertEquals("at index "+i, expectedOffsets[i], offsets[i], 0.);
        }
    }


    @Test
    public void test22() {
        Dimension<Integer> d = new Dimension<>(2, 2, a->(int)a );
        Dimension<Integer> cro = new Dimension<>(1, 1, a->(int)a );
        double[] offsets = load(d,cro);
        /*
        index
               0    1   
           ╔════╤════╗  
          0║   0│   1║  
           ╟────┼────╢  
          1║   2│   3║  
           ╚════╧════╝  
                        
        store           
               0    1   
           ╔════╤════╗  
          0║   1│   2║  
           ╟────┼────╢  
          1║   4│   8║  
           ╚════╧════╝  
        */
        Assert.assertEquals(" ", 5, offsets.length);
        double[] expectedOffsets = new double[] {
                1, 2, 
                4, 8,
        };
        for (int i = 0; i < expectedOffsets.length; i++) {
          Assert.assertEquals("at index "+i, expectedOffsets[i], offsets[i], 0.);
        }
    }

    @Test
    public void test31() {
        Dimension<Integer> d = new Dimension<>(3, 1, a->(int)a );
        Dimension<Integer> cro = new Dimension<>(1, 1, a->(int)a );
        double[] offsets = load(d,cro);

        /*
        index
                0    1    2   
            ╔════╤════╤════╗  
           0║   0│   1│   2║  
            ╚════╧════╧════╝  
                              
         store                
                0    1    3   
            ╔════╤════╤════╗  
           0║   1│   2│   4║  
            ╚════╧════╧════╝  
        */        
        Assert.assertEquals(" ", 4, offsets.length);
        double[] expectedOffsets = new double[] {
                1, 2, 4
        };
        for (int i = 0; i < expectedOffsets.length; i++) {
            Assert.assertEquals("at index "+i, expectedOffsets[i], offsets[i], 0.);
        }
    }

    @Test
    public void test52() {
        Dimension<Integer> d = new Dimension<>(5, 2, a->(int)a );
        Dimension<Integer> cro = new Dimension<>(2, 2, a->(int)a );
        double[] offsets = load(d,cro);
        /*
        index
               0    1    2    3    4   
           ╔════╤════╤════╤════╤════╗  
          0║   0│   1│   2│   3│   4║  
           ╟────┼────┼────┼────┼────╢  
          1║   5│   6│   7│   8│   9║  
           ╚════╧════╧════╧════╧════╝  
                                       
        store                          
               0    1    2    3    4   
           ╔════╤════╤════╤════╤════╗  
          0║   1│   2│   4│   8│  16║  
           ╚════╧════╧════╧════╧════╝  
         */
        Assert.assertEquals(" ", 5, offsets.length);
        double[] expectedOffsets = new double[] {
                1, 2, 4, 8, 16, 
        };
        for (int i = 0; i < expectedOffsets.length; i++) {
            Assert.assertEquals("at index "+i, expectedOffsets[i], offsets[i], 0.);
        }
    }
    
    @Test
    public void test35() {
        Dimension<Integer> d = new Dimension<>(3, 5, a->(int)a );
        Dimension<Integer> cro = new Dimension<>(1, 1, a->(int)a );
        double[] offsets = load(d,cro);
        
        Assert.assertEquals(" ", 16, offsets.length);
        double[] expectedOffsets = new double[] {
        };
        for (int i = 0; i < expectedOffsets.length; i++) {
            Assert.assertEquals("at index "+i, expectedOffsets[i], offsets[i], 0.);
        }
    }
    
    private double[] load(Dimension<Integer> D, Dimension<Integer> d) {
        final RectangleRandomizer randomizer = new RectangleRandomizer(
                D.w, D.h, 
                d.w, d.h,
                RectangleRandomizer.Type.UNIFORM);
        final Store store = (Store)randomizer.computer.store;

        long weight = 0;
        store.lazyLoad();
        for (int index = 0; index < store.unauthorizedGetSegmentsJustForTesting().length; index++) {
            weight = (long)Math.pow(2., (double)index);
            store.unauthorizedSetSegmentJustForTesting((int)index, weight);
        }
        return store.unauthorizedGetSegmentsJustForTesting();
    }

}
