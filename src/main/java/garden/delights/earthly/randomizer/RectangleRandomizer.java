package garden.delights.earthly.randomizer;

import static garden.delights.earthly.randomizer.RectangleRandomizer.Type.BASIC;
import static garden.delights.earthly.randomizer.RectangleRandomizer.Type.UNIFORM;

import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import garden.delights.earthly.randomizer.RectangleRandomizerUtil.Dimension;
import garden.delights.earthly.randomizer.RectangleRandomizerUtil.Point;
import garden.delights.earthly.randomizer.RectangleRandomizerUtil.Rectangle;

public class RectangleRandomizer implements RectangleRandomizerInterface  {

    @SuppressWarnings("unused")
    private final static Logger log = LoggerFactory.getLogger(RectangleRandomizer.class);

    final         Computer computer;
    final private Dimension<Long> D;
    final private Dimension<Long> d;
    
    final private ThreadLocalRandom threadLocalRandom;
    final         RectangleRandomizerInterface internal;

    public static enum Type {
        BASIC, UNIFORM
    }

    public RectangleRandomizer (
            final long sourceWidth, 
            final long sourceHeight, 
            final long targetWidth, 
            final long targetHeight,
            final Type type) {
        
        // ensure target no larger than source
        final long minWidth  = Math.min(sourceWidth , targetWidth);
        final long minHeight = Math.min(sourceHeight, targetHeight);

        this.D = new Dimension<Long>(sourceWidth, sourceHeight, a->(long)a );
        this.d = new Dimension<Long>(minWidth, minHeight, a->(long)a );
        
        this.computer = new Computer(this.D, this.d);
        
        this.threadLocalRandom = ThreadLocalRandom.current();

        if (type.equals(BASIC)){
            this.internal = new BasicRectangleRandomizer();
        } else if (type.equals(UNIFORM)){
            this.internal = new UniformRectangleRandomizer();
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    @Override
    public Rectangle<Long> getRandomRectangle() {
        return internal.getRandomRectangle();
    }

    /**
     * ABSTRACT
     */
    abstract class AbstractRectangleRandomizer implements RectangleRandomizerInterface {

        @Override
        public Rectangle<Long> getRandomRectangle() {
            int r = (int)randomDouble();
            if (r <= 0.) {
                return new Rectangle<>(0L, 0L, 0L, 0L, a->(long)a);
            }
            Point<Long>      p    = pointFromIndex(r);
            Rectangle<Long>  rect = new Rectangle<>(p, d);
            return rect;
        }

        abstract protected double randomDouble();
        abstract protected double map(final double d);

        protected Point<Long> pointFromIndex(final long index) {
            Point<Long> p = computer.biggest.pointFromIndex(index);
            return p;
        }

        protected long indexFromPoint(final Point<Long> p) {
            return computer.biggest.indexFromPoint(p);
        }
        
        protected long getSize() {
            return computer.biggest.getSize();
        }

    }

    /**
     * BASIC
     */
    class BasicRectangleRandomizer extends AbstractRectangleRandomizer implements RectangleRandomizerInterface {

        @Override
        protected double randomDouble() {
            final double    r       = threadLocalRandom.nextDouble();
            final Long      size    = getSize();
            return map( r * size );
        }
        
        @Override
        protected double map(final double d) {
            return d;
        }
    }

    /**
     * UNIFORM
     */
    class UniformRectangleRandomizer extends AbstractRectangleRandomizer {
        
        @Override
        protected double randomDouble() {
            final double    r       = threadLocalRandom.nextDouble();
            final double    size    = getWeightedSize();
            return map( r * size );
        }

        @Override
        protected double map(final double d) {
            return computer.store.map(d);
        }

        double getWeightedSize() {
            return computer.store.getSize();
        }

    }

}
