package garden.delights.earthly.imageserver.randomizer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

import garden.delights.earthly.imageserver.randomizer.Computer.Store;
import garden.delights.earthly.imageserver.randomizer.RectangleRandomizerUtil.Dimension;

public class ComputerTest {

    @Test
    public void test() throws IOException {
        
        final boolean writeToFile = true;  // false; // 
        final boolean keepFile    = false; // true;  // 
        
        Path path = null;
        if (writeToFile && System.getenv("HC_HOST") == null) {
            final Path otherPath = Files.createTempFile("computer-test-", ".txt");
            path = otherPath;
            System.out.println("output of " + ComputerTest.class.getSimpleName() + " written to tmp file : " + path);
            if (!keepFile) path.toFile().deleteOnExit();

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        if (!keepFile) {
                            Files.delete(otherPath);
                            System.out.println("deleted file at " + otherPath);
                        } else {
                            System.out.println("NOT deleted file at " + otherPath);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
      
        try (OutputStream fos = path==null ? null : new FileOutputStream(path.toFile())){
            PrintStream sysout;
            if (fos == null) {
                sysout = System.out; 
            } else {
                sysout = new PrintStream(fos);
            }
            final int from_X = 12;
            final int to_X   = 12;
            final int from_Y = 12;
            final int to_Y   = 12;
            final int from_x = 5;
            final int to_x   = 5;
            final int from_y = 5;
            final int to_y   = 5;
            
            int level = 1;
            for (long H = from_Y; H <= to_Y; H++) {
                separator(sysout, level++);
                for (long W = from_X; W <= to_X; W++) {
                    separator(sysout, level++);
                    for (long h = from_y; h <= to_y; h++){
                        separator(sysout, level++);
                        for (long w = from_x; w <= to_x; w++){
                            separator(sysout, level++);
                            sysout.println("H="+H+"\tW="+W+"\th="+h+"\tw="+w+"\n");
                            Dimension<Long> big   = new Dimension<Long>(W, H, a->(long)a);
                            Dimension<Long> small = new Dimension<Long>(w, h, a->(long)a);
                            Computer        c     = new Computer(big, small);
                            
                            c.store.getSize(); // will trigger lazy loading
                            
                            draw(c, Computer.Function.MIRROR, sysout);
                            draw(c, Computer.Function.WEIGHT_OF_CROP, sysout);
                            
                            --level;
                        }
                        --level;
                    }
                    --level;
                }
                --level;
            }
        } catch (Exception e) {
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
    
    private void draw(final Computer c, final Computer.Function f, final PrintStream sysout) {
        if (f.equals(Computer.Function.MULTIPLY)) {
            sysout.print(f.toString().toLowerCase());
            sysout.print(" big ");
            sysout.print(c.big);
            sysout.print(" ");
        }
        if (f.equals(Computer.Function.MIRROR)) {
            sysout.print(f.toString().toLowerCase());
            sysout.print(" small ");
            sysout.print(c.small);
            sysout.print(" ");
        }
        final long width; 
        final long height;
        if (f.equals(Computer.Function.WEIGHT_OF_CROP)) {
            sysout.print(f.toString().toLowerCase());
            sysout.print(" biggest ");
            sysout.print(c.biggest);
            sysout.print(" ");
            width  = c.biggest.w;
            height = c.biggest.h;
        } else {
            width = c.big.w; 
            height = c.big.h;
        }
        sysout.println("");
        
        matrix(c.operations.get(f), width, height, sysout);

        if (f.equals(Computer.Function.WEIGHT_OF_CROP)) {
            Computer.F adjustment = (a,b) -> (
                    (long)(10000.*((Store)c.store).getAdjustment((int)(b*width + a)))
            );
            matrix(adjustment, width, height, sysout);
            Computer.F adjusted = (a,b) -> ((long)(
                    ((Store)c.store).getAdjustment((int)(b*width + a))
                    * 100. *
                    c.operations.get(f).get(a,b))
            );
            matrix(adjusted, width, height, sysout);
        }

    }

    private void matrix(final Computer.F f, final long width, final long height, final PrintStream sysout) {
        long sum = 0;
        for (long y=0; y<height; y++) {
            for (long x=0; x<width; x++) {
                if (x==0){
                    if (y==0) {
                        sysout.print(       String.format("%5s |",      "."         ));
                        for (long X=0; X<width; X++) {
                            sysout.print(   String.format("\t%5d",      X           ));
                        }
                        sysout.println();
                        sysout.print(       String.format("──────%s",   "┼"         ));
                        for (long i=0; i<width; i++) {
                            sysout.print("────────");
                        }
                        sysout.println();
                    }
                    sysout.print(           String.format("%5d │",      y           ));
                }
                long l = f.get(x, y);
                sum += l;
                sysout.print(               String.format("\t%5d",      l ));
            }
            sysout.println();
        }
        sysout.print("\t");
        for (long X=0; X<width; X++) sysout.print("\t");
        sysout.println("=" + sum);
        sysout.println();
    }
}
