package garden.delights.earthly.randomizer;

import org.junit.Assert;
import org.junit.Test;

import garden.delights.earthly.randomizer.Computer;
import garden.delights.earthly.randomizer.RectangleRandomizer;
import garden.delights.earthly.randomizer.Computer.Store;
import garden.delights.earthly.randomizer.RectangleRandomizerUtil.Dimension;

public class ComputerStoreTest {

    @Test
    public void test00() {
        Dimension<Integer> d = new Dimension<>(0, 0, a->(int)a );
        Computer.Store store = load(d,d);
        Assert.assertEquals(" ", 1, store.getOffsetsJustForTesting().length);
        Assert.assertEquals("at index 0", 1., store.get(0).weight, 0.);
    }

    @Test
    public void test11() {
        Dimension<Integer> d = new Dimension<>(1, 1, a->(int)a );
        Computer.Store store = load(d,d);
        store.test = true;
        Assert.assertEquals(" ", 2, store.getOffsetsJustForTesting().length);
        Assert.assertEquals("at index 0", 1, store.get(0).weight, 0.);
        Assert.assertEquals("at index 1", 2, store.get(1).weight, 0.);
    }

    @Test
    public void test21() {
        Dimension<Integer> d = new Dimension<>(2, 1, a->(int)a );
        Dimension<Integer> cro = new Dimension<>(1, 1, a->(int)a );
        Computer.Store store = load(d,cro);

        Assert.assertEquals(" ", 3, store.getOffsetsJustForTesting().length);
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
            Assert.assertEquals("at index "+i, expectedOffsets[i], store.get(i).weight, 0.);
        }
    }

    @Test
    public void test22() {
        Dimension<Integer> d = new Dimension<>(2, 2, a->(int)a );
        Dimension<Integer> cro = new Dimension<>(1, 1, a->(int)a );
        Computer.Store store = load(d,cro);
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
        Assert.assertEquals(" ", 5, store.getOffsetsJustForTesting().length);
        double[] expectedOffsets = new double[] {
                1, 2, 
                4, 8,
        };
        for (int i = 0; i < expectedOffsets.length; i++) {
            Assert.assertEquals("at index "+i, expectedOffsets[i], store.get(i).weight, 0.);
        }
    }

    @Test
    public void test31() {
        Dimension<Integer> d = new Dimension<>(3, 1, a->(int)a );
        Dimension<Integer> cro = new Dimension<>(1, 1, a->(int)a );
        Computer.Store store = load(d,cro);

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
        Assert.assertEquals(" ", 4, store.getOffsetsJustForTesting().length);
        double[] expectedOffsets = new double[] {
                1, 2, 4
        };
        for (int i = 0; i < expectedOffsets.length; i++) {
            Assert.assertEquals("at index "+i, expectedOffsets[i], store.get(i).weight, 0.);
        }
    }

    @Test
    public void test52() {
        Dimension<Integer> d = new Dimension<>(5, 2, a->(int)a );
        Dimension<Integer> cro = new Dimension<>(2, 2, a->(int)a );
        Computer.Store store = load(d,cro);
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
        Assert.assertEquals(" ", 5, store.getOffsetsJustForTesting().length);
        double[] expectedOffsets = new double[] {
                1, 2, 4, 8, 16, 
        };
        for (int i = 0; i < expectedOffsets.length; i++) {
            Assert.assertEquals("at index "+i, expectedOffsets[i], store.get(i).weight, 0.);
        }
    }
    
    @Test
    public void test35() {
        Dimension<Integer> d = new Dimension<>(3, 5, a->(int)a );
        Dimension<Integer> cro = new Dimension<>(1, 1, a->(int)a );
        Computer.Store store = load(d,cro);
        
        Assert.assertEquals(" ", 16, store.getOffsetsJustForTesting().length);
        double[] expectedOffsets = new double[] {
        };
        for (int i = 0; i < expectedOffsets.length; i++) {
            Assert.assertEquals("at index "+i, expectedOffsets[i], store.get(i).weight, 0.);
        }
    }
    
    private Computer.Store load(Dimension<Integer> D, Dimension<Integer> d) {
        final RectangleRandomizer randomizer = new RectangleRandomizer(
                D.w, D.h, 
                d.w, d.h,
                RectangleRandomizer.Type.UNIFORM);
        final Store store = (Store)randomizer.computer.store;

        long weight = 0;
        store.test = true;
        store.lazyLoad();
        for (int index = 0; index < store.getOffsetsJustForTesting().length; index++) {
            weight = (long)Math.pow(2., (double)index);
            store.unauthorizedModifyWeightJustForTesting((int)index, weight);
        }
        return store;
    }

}
