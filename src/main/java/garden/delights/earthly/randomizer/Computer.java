package garden.delights.earthly.randomizer;

import java.util.EnumMap;

import garden.delights.earthly.randomizer.RectangleRandomizerUtil.Dimension;

/*
╔═ big ═════════════════════════════╗          ╔═ big ═════════════════════════════╗ ^   
║                                   ║          ║             |                     ║ |   
║                                   ║          ║  smallest   |                     ║ | β 
║                                   ║          ║             |                     ║ |   
║                                   ║          ║ - - - - - - ┌─ small ─────────────╢ v   
║                                   ║          ║             │                     ║     
║                                   ║          ║             │                     ║     
║                                   ║          ║             │                     ║     
║                     ┌─ small ─────╢ ^        ║             │                     ║     
║                     │             ║ |        ║             │                     ║     
║                     │  smallest   ║ | β      ║             │                     ║     
║                     │             ║ |        ║             │                     ║     
╚═════════════════════╧═════════════╝ v        ╚═════════════╧═════════════════════╝     
                      <------------->          <------------->                           
                            α                         α                                  
*/

class Computer {
    final Dimension<Long>       big;
    final Dimension<Long>       small;
    final Dimension<Long>       smallest;
    final Dimension<Long>       biggest;
    final EnumMap<Functions, F> operations;
    final MapInterface        store;
          
    private long                totalWeight;

    Computer(Dimension<Long> big, Dimension<Long> small) {
        this.big            = big;
        this.small          = small;
        long α              = Math.min(this.small.w, this.big.w - this.small.w);
        long β              = Math.min(this.small.h, this.big.h - this.small.h);
        this.smallest       = new Dimension<Long>(α, β, a->(long)a); 
        final long times_h  = this.small.w == 0 ? 0L : 1 + (this.big.w - this.small.w);
        final long times_v  = this.small.h == 0 ? 0L : 1 + (this.big.h - this.small.h);
        this.biggest        = new Dimension<Long>(times_h, times_v, a->(long)a); 
        this.operations     = new EnumMap<Computer.Functions, Computer.F>(Computer.Functions.class);
        this.store          = new Store();
        
        operations.put(Functions.MULTIPLY,          multiply);
        operations.put(Functions.MIRROR,            mirror);
        operations.put(Functions.WEIGHT_OF_CROP,    weightOfCrop);
    }

    final int C = 1;
    final int D = 1;
    
    final F multiply = (a, b) -> (a + 1) * (b + 1);             //  multiply big = (5x5)                                        
                                                                //                                                              
    final F mirror = (x, y) -> {                                //      x * y |     0       1       2       3       4           
        long a, b;                                              //      ──────┼─────────────────────────────────────────────    
                                                                //          0 │     1       2       3       4       5           
        if (x < this.smallest.w) {                              //          1 │     2       4       6       8      10           
            a = x;                                              //          2 │     3       6       9      12      15           
        } else if (this.big.w - this.smallest.w - 1 < x) {      //          3 │     4       8      12      16      20           
            a = this.big.w - x - 1;                             //          4 │     5      10      15      20      25           
        } else {                                                //                                                              
            a = this.smallest.w - 1;                            //  mirror smallest = (2x2)                                        
        }                                                       //                                                              
                                                                //       frqs |     0       1       2       3       4           
        if (y < this.smallest.h) {                              //      ──────┼─────────────────────────────────────────────    
            b = y;                                              //          0 │     1       2       2       2       1           
        } else if (this.big.h - this.smallest.h - 1 < y) {      //          1 │     2       4       4       4       2           
            b = this.big.h - y - 1;                             //          2 │     2       4       4       4       2           
        } else {                                                //          3 │     2       4       4       4       2           
            b = this.smallest.h - 1;                            //          4 │     1       2       2       2       1           
        }                                                       //                                                              
        if (a < 0) {
            a = 0;
        }
        if (b < 0) {
            b = 0;
        }
                                                                //  weightOfCrop  = crop 2x2                                    
        return multiply.get(a, b);                              //                                                              
    };                                                          //      integ |     0       1                                   
                                                                //      ──────┼──────────────────                               
    final F weightOfCrop = (a, b) -> {                          //          0 │     9      12                                   
        long ret = 0L;                                          //          1 │    12      16                                   
        for (long i=a; i<a+this.small.w; i++) {                 //                                                              
            for (long j=b; j<b+this.small.h; j++) {             //                                                              
                ret += mirror.get(i, j);                        //                                                              
            }                                                   //                                                              
        }                                                       //                                                              
        return ret;                                             //                                                              
    };                                                          //                                                              
    
    @FunctionalInterface
    interface F {
        long get(long x, long y);
    }
    
    enum Functions {
        MULTIPLY("x * y"), MIRROR("frqs"), WEIGHT_OF_CROP("crop");
        final String desc;
        Functions(String desc) {
            this.desc = desc;
        }
    };
    
    long getTotalWeight() {
        if (this.totalWeight == 0) { // not yet cached
            for (long j=0; j<biggest.h; j++) {
                for (long i=0; i<biggest.w; i++) {                                                              
                    final long weightOfCrop = Computer.this.weightOfCrop.get(i,j);
                    this.totalWeight += weightOfCrop;
                }
            }
        }
        return this.totalWeight; 
    }
    
    class WeightIntegraleAndSegment {
        @Override
        public String toString() {
            return "[" + weight + ", " + integrale + ", " + segment + "]";
        }
        long   weight;
        long   integrale;  // not actually needed, makes debugging easier
        double segment;
        WeightIntegraleAndSegment(long weight, long integrale, double segment) {
            super();
            this.weight  = weight;
            this.integrale = integrale;
            this.segment = segment;
        }
    }
    
    interface MapInterface {
        double getSize();
        double map(double d);
    }
    
    class Store implements MapInterface {
        private WeightIntegraleAndSegment[] offsets;

        // hack for test
        boolean test;

        /**
         * lazyLoad should be called internally only from public interfaces (i.e. getSize and map), 
         * tests using other methods with package visibility must explicitly call lazyLoad before doing anything
         */
        void lazyLoad() {
            if (this.offsets == null) {
                this.offsets = new WeightIntegraleAndSegment[1 + (int)(biggest.w * biggest.h)];
                {
                    int index = 0;
                    long integrale = 0;
                    for (long j=0; j<biggest.h; j++) {
                        for (long i=0; i<biggest.w; i++) {                                                              
                            final long weight = Computer.this.weightOfCrop.get(i,j);
                            integrale += weight;
                            put(index, new WeightIntegraleAndSegment(
                                weight,
                                integrale,
                                0.
                            ));
                            index++;
                        }
                    }
                    assert(index == this.offsets.length-1);
                    this.offsets[index] = new WeightIntegraleAndSegment(integrale, integrale, 1.);
                }

                {
                    int index = 0;
                    if (0 < this.offsets[this.offsets.length-1].integrale) {
                        
                        double totalWeight = (double)offsets[offsets.length-1].integrale;
                        double segment = 0;
                        for (long j=0; j<biggest.h; j++) {
                            for (long i=0; i<biggest.w; i++) {                                                              
                                modifySegment(index,  segment);
                                double thisSegment = offsets[index].weight/totalWeight;
                                double adjustedSegment = 1./(100.*thisSegment);
                                segment += adjustedSegment;
                                index++;
                            }
                        }
                        modifySegment(index,  segment);
                    }
                }
            }
        }

        @Override
        public double getSize() {
            lazyLoad(); 
            return this.offsets[this.offsets.length-1].segment;
        }
        
        @Override
        public double map(double d) {
            // lazyLoad(); // clients must call getFarthestSegment before dichotomize
            double ret = 0.;

            int first  = 0;
            int last   = this.offsets.length - 1;
            int middle = (first + last)/2;

            while ( first <= last ) {
                if (middle == last) {
                    ret = middle;
                    break;
                }

                if ( d < this.offsets[middle].segment ) {
                    last = middle - 1;

                } else if ( d < this.offsets[middle + 1].segment ) {
                    ret = middle;
                    break;
                } else {
                    first = middle + 1;
                }
                middle = (first + last)/2;
            }
            return ret;
        }

        private void put(int index, WeightIntegraleAndSegment both) {
            this.offsets[index] = both;
        }

        private void modifySegment(int index, double segment) {
            this.offsets[index].segment = segment;
        }

        void unauthorizedModifyWeightJustForTesting(int index, long weight) {
            this.offsets[index].weight = weight;
        }
        
        WeightIntegraleAndSegment get(int index) {
            return this.offsets[index];
        }

        WeightIntegraleAndSegment[] getOffsetsJustForTesting() {
            return this.offsets;
        }
    }
}