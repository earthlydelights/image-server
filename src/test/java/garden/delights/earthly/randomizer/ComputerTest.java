package garden.delights.earthly.randomizer;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;

import garden.delights.earthly.randomizer.Computer;
import garden.delights.earthly.randomizer.Computer.F;
import garden.delights.earthly.randomizer.RectangleRandomizerUtil.Dimension;

public class ComputerTest {

    static String dash = "-";
    
    @FunctionalInterface
    interface F2 {
        long get(long X, long Y, long x, long y);
    }
    
    final F2 integrale = (a,b,c,d) -> ( c * c * d / 2) / ( a * a * b / 2);

    @Test
    public void test0() {
        Dimension<Long> big   = new Dimension<Long>(5L, 5L, a->(long)a);
        Dimension<Long> small = new Dimension<Long>(2L,5L, a->(long)a);
        System.out.println(integrale.get(5L, 5L, 5L, 5L));
    }


    // @Test
    public void test() throws IOException {
        /* File file = new File("dugenou.txt"); */

        final Path path = Files.createTempFile("geppaequo-computer-test", ".txt");
        System.out.println("Temp file : " + path);
        // path.toFile().deleteOnExit();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    // Files.delete(path);
                    // System.out.println("deleted file at " + path);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
      
        try (OutputStream fos = new FileOutputStream(path.toFile())){
            PrintStream sysout = System.out; // new PrintStream(fos); // 
            
            final long SQUARE = 5;

            int level = 1;
            for (long H = 5; H <= SQUARE; H++) {
                separator(sysout, level++);
                for (long W = 5; W <= SQUARE; W++) {
                    separator(sysout, level++);
                    for (long h = 2; h <= H; h++){
                        separator(sysout, level++);
                        for (long w = 2; w <= W; w++){
                            separator(sysout, level++);
                            sysout.println("H="+H+"\tW="+W+"\th="+h+"\tw="+w+"\n");
                            Dimension<Long> big   = new Dimension<Long>(W, H, a->(long)a);
                            Dimension<Long> small = new Dimension<Long>(w, h, a->(long)a);
                            Computer c = new Computer(big, small);
                            draw(c, Computer.Functions.MIRROR, sysout);
                            draw(c, Computer.Functions.WEIGHT_OF_CROP, sysout);
                            --level;
                        }
                        --level;
                    }
                    --level;
                }
                --level;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void separator(PrintStream sysout, int level) {
        switch (level) {
        case 1:
            sysout.println("################################################################################");
            break;

        case 2:
            sysout.println("////////////////////////////////////////////////////////////////////////////////");
            break;

        case 3:
            sysout.println("--------------------------------------------------------------------------------");
            break;

        case 4:
            sysout.println(". . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . ");
            break;

        default:
            break;
        }
    }
    
    public void draw(Computer c, Computer.Functions f, PrintStream sysout) {
        if (f.equals(Computer.Functions.MULTIPLY)) {
            sysout.print(f.toString().toLowerCase());
            sysout.print(" big ");
            sysout.print(c.big);
            sysout.print(" ");
        }
        if (f.equals(Computer.Functions.MIRROR)) {
            sysout.print(f.toString().toLowerCase());
            sysout.print(" small ");
            sysout.print(c.small);
            sysout.print(" ");
        }
        long width = c.big.w; 
        long height = c.big.h;
        if (f.equals(Computer.Functions.WEIGHT_OF_CROP)) {
            width  = c.biggest.w;
            height = c.biggest.h;
            sysout.print(f.toString().toLowerCase());
            sysout.print(" biggest ");
            sysout.print(c.biggest);
            sysout.print(" ");
        }
        sysout.println("\n");
        
        Multiset<Long> cumul = TreeMultiset.<Long>create();
        for (long y=0; y<height; y++) {
            for (long x=0; x<width; x++) {
                if (x==0){
                    if (y==0) {
                        sysout.print(String.format("\t%5s |", f.desc));
                
                        for (long i=0; i<width; i++) {
                            sysout.print(String.format("\t%5d", i));
                        }
                        sysout.println();
                        sysout.print(String.format("\t──────┼", "+"));
                        
                        for (long i=0; i<width; i++) {
                            sysout.print("─────────");
                        }
                        sysout.println();
                    }
                    sysout.print(String.format(" \t%5d │", y));
                }
                long ret = c.operations.get(f).get(x, y);
            
                cumul.add(ret);
            
                sysout.print(String.format("\t%5d", ret));
            }
            sysout.println();
        }
        sysout.println();
        if (f.equals(Computer.Functions.WEIGHT_OF_CROP)) {
           for (Long cropWeight : cumul.elementSet()) {
                sysout.println(cropWeight + " " + cumul.count(cropWeight));
           }
        }
        sysout.println();
        
    }

}
