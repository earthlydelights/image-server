package garden.delights.earthly.imageserver.randomizer;

import java.util.EnumMap;

import garden.delights.earthly.imageserver.randomizer.RectangleRandomizerUtil.Dimension;

/*
                                               |                                                   
╔═ big ═════════════════════════════╗     ^    |     ╔═ big ═════════════════════════════╗ ^   ^   
║                     |             ║     |    |     ║  smallest   |                     ║ |   |   
║                     |             ║     |    |     ║    ~=       |                     ║ | β | δ 
║                     |             ║     |    |     ║  biggest    |                     ║ v   |   
║       biggest       |             ║     | δ  |     ║-------------┌─ small ─────────────╢     v   
║                     |             ║     |    |     ║             │                     ║         
║                     |             ║     |    |     ║             │                     ║         
║                     |             ║     |    |     ║             │                     ║         
║---------------------┌─ small ─────╢     v    |     ║             │                     ║         
║                     │             ║ ^        |     ║             │                     ║         
║                     │  smallest   ║ | β      |     ║             │                     ║         
║                     │             ║ |        |     ║             │                     ║         
╚═════════════════════╧═════════════╝ v        |     ╚═════════════╧═════════════════════╝         
                       <------------>          |     <------------>                                
                            α                  |            α                                      
<--------------------->                        |     <------------->                               
          γ                                    |            γ                                      
                                               |                                                   
*/

class Computer {
    final Dimension<Long>       big;
    final Dimension<Long>       small;
    final Dimension<Long>       smallest;
    final Dimension<Long>       biggest;
    
    final EnumMap<Functions, F> operations;
    final MapInterface          store;
          
    Computer(Dimension<Long> big, Dimension<Long> small) {
        this.big            = big;
        this.small          = small;
        final long α        = Math.min(this.small.w, this.big.w - this.small.w);
        final long β        = Math.min(this.small.h, this.big.h - this.small.h);
        this.smallest       = new Dimension<Long>(α, β, a->(long)a); 
        final long γ        = this.small.w <= 0 ? 0L : 1 + (this.big.w - this.small.w);
        final long δ        = this.small.h <= 0 ? 0L : 1 + (this.big.h - this.small.h);
        this.biggest        = new Dimension<Long>(γ, δ, a->(long)a); 
        this.operations     = new EnumMap<Computer.Functions, Computer.F>(Computer.Functions.class);
        this.store          = new Store();
        
        operations.put(Functions.MULTIPLY,          multiply);
        operations.put(Functions.MIRROR,            mirror);
        operations.put(Functions.WEIGHT_OF_CROP,    weightOfCrop);
    }

    @FunctionalInterface
    interface F {
        long get(long x, long y);
    }

    //----------------------------------------------------------////////////////////////////////////////////////////////////////
    final F multiply = (a, b) -> (a + 1) * (b + 1);             //  multiply big = (5x5)                                        
                                                                //                                                              
                                                                //      x * y |     0       1       2       3       4           
                                                                //      ──────┼─────────────────────────────────────────────    
                                                                //          0 │     1       2       3       4       5           
                                                                //          1 │     2       4       6       8      10           
                                                                //          2 │     3       6       9      12      15           
                                                                //          3 │     4       8      12      16      20           
                                                                //          4 │     5      10      15      20      25           
                                                                //                                                              

    //----------------------------------------------------------///////////////////////////////////////////////////////////////
    final F mirror = (x, y) -> {                                //  mirror smallest = (2x2)                                    
        long a, b;                                              //                                                             
                                                                //       frqs |     0       1       2       3       4          
        if (x < this.smallest.w) {                              //      ──────┼─────────────────────────────────────────────   
            a = x;                                              //          0 │     1       2       2       2       1          
        } else if (this.big.w - this.smallest.w - 1 < x) {      //          1 │     2       4       4       4       2          
            a = this.big.w - x - 1;                             //          2 │     2       4       4       4       2          
        } else {                                                //          3 │     2       4       4       4       2          
            a = this.smallest.w - 1;                            //          4 │     1       2       2       2       1          
        }

        if (y < this.smallest.h) {
            b = y;
        } else if (this.big.h - this.smallest.h - 1 < y) {
            b = this.big.h - y - 1;
        } else {
            b = this.smallest.h - 1;
        }
        if (a < 0) {
            a = 0;
        }
        if (b < 0) {
            b = 0;
        }

        return multiply.get(a, b);
    };
                                    
    //----------------------------------------------------------/////////////////////////////////////////////////////
    final F weightOfCrop = (a, b) -> {                          //  weightOfCrop  = crop 2x2                         
        long ret = 0L;                                          //                                                   
        for (long i=0; i < this.small.w; i++) {                 //        weight |     0       1       2       3     
            for (long j=0; j < this.small.h; j++) {             //         ──────┼─────────────────────────────────  
                ret += mirror.get(a + i, b + j);                //             0 │     9      12      12       9     
            }                                                   //             1 │    12      16      16      12     
        }                                                       //             2 │    12      16      16      12     
        return ret;                                             //             3 │     9      12      12       9     
    };                                                          //                                                   
    

    enum Functions {
        MULTIPLY("x * y"), MIRROR("frqs"), WEIGHT_OF_CROP("crop");
        final String desc;
        Functions(String desc) {
            this.desc = desc;
        }
    };
    
    interface MapInterface {
        double getSize();
        double map(double d);
    }
    
    class Store implements MapInterface {
        private double[] segments;
        private long totalWeight;

        /**
         * lazyLoad should be called internally only from public interfaces (i.e. getSize and map)
         * actually, only getSize, as map is called a large number of time, so to reduce overhead, map, although public, does not call lazyload.
         * tests testing other methods (i.e. methods having package visibility) should explicitly call lazyLoad before doing anything
         */
        void lazyLoad() {
            if (this.segments == null) {
                final int length = (int)(biggest.w * biggest.h);
                
                this.segments = new double[length + 1];
                
                // 1st iteration : calculate total weight & store temporarily each weight in segment array (a bit of a hack, agreed) 
                {
                    int index = 0;
                    for (long j=0; j<biggest.h; j++) {
                        for (long i=0; i<biggest.w; i++) {                                                              
                            final long weight = Computer.this.weightOfCrop.get(i,j);
                            this.segments[index] = weight;
                            this.totalWeight += weight;
                            index++;
                        }
                    }
                }
                
                // 2nd iteration : calculate segments as a function of totalWeight and weight (saved during 1st iteration above, and irremediably lost after 2nd iteration below)
                this.segments[length] = 0; // last segment stores the ... err ... last segment
                double weight_of_previous_segment = this.segments[0];  // hack
                for (int i = 1; i <= length; i++) {
                    final double weight = this.segments[i];
                    this.segments[i] = this.segments[length] += (double)this.totalWeight / weight_of_previous_segment; 
                    weight_of_previous_segment = weight;
                }
                this.segments[0] = 0.;
            }
        }

        @Override
        public double getSize() {
            lazyLoad(); 
            return this.segments[this.segments.length-1];
        }
        
        @Override
        public double map(double d) {
            // lazyLoad(); // clients MUST call getSize before map
            double ret = 0.;

            // dichotomize
            int first  = 0;
            int last   = this.segments.length-1;
            int middle = (first + last)/2;

            while ( first <= last ) {
                if (middle == last) {
                    ret = middle;
                    break;
                }
                if ( d < this.segments[middle]) {
                    last = middle - 1;
                } else if ( d < this.segments[middle + 1]) {
                    ret = middle;
                    break;
                } else {
                    first = middle + 1;
                }
                middle = (first + last)/2;
            }
            return ret;
        }

        void unauthorizedSetSegmentJustForTesting(int index, long segment) {
            this.segments[index] = segment;
        }
        
        double[] unauthorizedGetSegmentsJustForTesting() {
            return this.segments;
        }
        
        long unauthorizedGetTotalWeightJustForTesting() {
            return this.totalWeight;
        }

    }

}