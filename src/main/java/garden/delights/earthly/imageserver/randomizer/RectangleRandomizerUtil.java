package garden.delights.earthly.imageserver.randomizer;

public class RectangleRandomizerUtil {

    @FunctionalInterface
    public static interface Converter<T extends Number> {
        T valueOf(Number d);
    }

    public static class Point <T extends Number> {
        final T x;
        final T y;
        final Converter<T> convert;
        public Point(final T x, final T y, Converter<T> convert) {
            this.x = x;
            this.y = y;
            this.convert = convert;
        }
        final T hypothenuse() {
            double hypot = Math.hypot(x.doubleValue(), y.doubleValue());
            if (x instanceof Integer) {
                return convert.valueOf((int)hypot);
            } else if (x instanceof Float) {
                return convert.valueOf((float)hypot);
            } else if (x instanceof Double) {
                return convert.valueOf(hypot);
            } else if (x instanceof Byte) {
                return convert.valueOf((byte)hypot);
            } else if (x instanceof Short) {
                return convert.valueOf((short)hypot);
            } else if (x instanceof Long) {
                return convert.valueOf((long)hypot);
            } else {
                throw new UnsupportedOperationException();
            }
        }
        @Override
        public String toString() {
            return "Point(" + this.x + "," + this.y + ")";
        }
    }

    public static class Dimension <T extends Number> {
        final T w;
        final T h;
        final Converter<T> convert;
        public Dimension(final T w, final T h, Converter<T> convert) {
            this.w = w;
            this.h = h;
            this.convert = convert;
        }
        public Dimension<T> increment() {
            Number x;
            Number y;
            if (w instanceof Integer) {
                x = w.intValue() + 1;
                y = h.intValue() + 1;
            } else if (w instanceof Long) {
                x = w.longValue() + 1L;
                y = h.longValue() + 1L;
            } else if (w instanceof Double) {
                x = w.doubleValue() + 1.;
                y = h.doubleValue() + 1.;
            } else if (w instanceof Float) {
                x = w.floatValue() + 1f;
                y = h.floatValue() + 1f;
            } else if (w instanceof Byte) {
                x = w.byteValue() + 1;
                y = h.byteValue() + 1;
            } else if (w instanceof Short) {
                x = w.shortValue() + 1;
                y = h.shortValue() + 1;
            } else {
                throw new UnsupportedOperationException();
            }
            return new Dimension<T>(convert.valueOf(x), convert.valueOf(y), convert);
        }
        public Dimension<T> incrementX() {
            Number x;
            if (w instanceof Integer) {
                x = w.intValue() + 1;
            } else if (w instanceof Long) {
                x = w.longValue() + 1L;
            } else if (w instanceof Double) {
                x = w.doubleValue() + 1.;
            } else if (w instanceof Float) {
                x = w.floatValue() + 1f;
            } else if (w instanceof Byte) {
                x = w.byteValue() + 1;
            } else if (w instanceof Short) {
                x = w.shortValue() + 1;
            } else {
                throw new UnsupportedOperationException();
            }
            return new Dimension<T>(convert.valueOf(x), h, convert);
        }
        public Dimension<T> incrementY() {
            Number y;
            if (w instanceof Integer) {
                y = h.intValue() + 1;
            } else if (w instanceof Long) {
                y = h.longValue() + 1L;
            } else if (w instanceof Double) {
                y = h.doubleValue() + 1.;
            } else if (w instanceof Float) {
                y = h.floatValue() + 1f;
            } else if (w instanceof Byte) {
                y = h.byteValue() + 1;
            } else if (w instanceof Short) {
                y = h.shortValue() + 1;
            } else {
                throw new UnsupportedOperationException();
            }
            return new Dimension<T>(w, convert.valueOf(y), convert);
        }
        public T getSize() {
            if (w instanceof Integer) {
                return convert.valueOf(w.intValue() * h.intValue());
            } else if (w instanceof Float) {
                return convert.valueOf((float)Math.ceil(w.floatValue()) * (float)Math.ceil(h.floatValue()));
            } else if (w instanceof Double) {
                return convert.valueOf((double)Math.ceil(w.doubleValue()) * (double)Math.ceil(h.doubleValue()));
            } else if (w instanceof Byte) {
                return convert.valueOf(w.byteValue() * h.byteValue());
            } else if (w instanceof Short) {
                return convert.valueOf(w.shortValue() * h.shortValue());
            } else if (w instanceof Long) {
                return convert.valueOf(w.longValue() * h.longValue());
            } else {
                throw new UnsupportedOperationException();
            }
        }
        public Point<T> pointFromIndex(T index) {
            return pointFromIndex(index, this, null);
        }
        public Point<T> pointFromIndex(T index, Dimension<T>dim, Converter<T> convert) {
            if (convert == null) {
                convert = this.convert;
            }
            final Point<T> p;
            if (index instanceof Integer) {
                p = new Point<T>( convert.valueOf(index.intValue() % dim.w.intValue()),
                                  convert.valueOf((int)Math.floor(index.intValue() / dim.w.intValue())),
                                  convert);
            } else if (index instanceof Float) {
                p = new Point<T>( convert.valueOf(index.floatValue() % dim.w.floatValue()),
                                  convert.valueOf((float)Math.floor(index.floatValue() / dim.w.floatValue())),
                                  convert);
            } else if (index instanceof Double) {
                p = new Point<T>( convert.valueOf(index.doubleValue() % dim.w.doubleValue()),
                                  convert.valueOf((double)Math.floor(index.doubleValue() / dim.w.doubleValue())),
                                  convert);
            } else if (index instanceof Byte) {
                p = new Point<T>( convert.valueOf(index.byteValue() % dim.w.byteValue()),
                                  convert.valueOf((byte)Math.floor(index.byteValue() / dim.w.byteValue())),
                                  convert);
            } else if (index instanceof Short) {
                p = new Point<T>( convert.valueOf(index.shortValue() % dim.w.shortValue()),
                                  convert.valueOf((short)Math.floor(index.shortValue() / dim.w.shortValue())),
                                  convert);
            } else if (index instanceof Long) {
                p = new Point<T>( convert.valueOf(index.longValue() % dim.w.longValue()),
                                  convert.valueOf((long)Math.floor(index.longValue() / dim.w.longValue())),
                                  convert);
            } else {
                throw new UnsupportedOperationException();
            }
            if (p.x.doubleValue() >= dim.w.doubleValue() ||
                p.y.doubleValue() >= dim.h.doubleValue()) {
                throw new IllegalArgumentException(p.toString() + " not in "+dim);
            }
            return p;
        }
        public T indexFromPoint(Point<T> p) {
            return indexFromPoint(p, this);
        }
        public T indexFromPoint(Point<T> p, Dimension<T>dim) {
            if (p.x.doubleValue() >= dim.w.doubleValue() ||
                p.y.doubleValue() >= dim.h.doubleValue()) {
                throw new IllegalArgumentException(p.toString() + " not in "+dim);
            }
            if (p.x instanceof Integer) {
                return convert.valueOf(p.y.intValue() * dim.w.intValue() + p.x.intValue());
            } else if (p.x instanceof Float) {
                return convert.valueOf(Math.round(p.y.floatValue() * Math.round(dim.w.floatValue()) + p.x.floatValue()));
            } else if (p.x instanceof Double) {
                return convert.valueOf(p.y.doubleValue() * Math.round(dim.w.doubleValue()) + p.x.doubleValue());
            } else if (p.x instanceof Byte) {
                return convert.valueOf(p.y.byteValue() * dim.w.byteValue() + p.x.byteValue());
            } else if (p.x instanceof Short) {
                return convert.valueOf(p.y.shortValue() * dim.w.shortValue() + p.x.shortValue());
            } else if (p.x instanceof Long) {
                return convert.valueOf(p.y.longValue() * dim.w.longValue() + p.x.longValue());
            } else {
                throw new UnsupportedOperationException();
            }
        }
        Dimension<T> clone(Dimension<Double>scale){
            if (scale == null || (scale.w == 1. && scale.h == 1.)) {
                return new Dimension<T>(w, h, convert);
            } else {
                if (w instanceof Integer) {
                    return new Dimension<T>(
                            convert.valueOf(   (int)(scale.w * w.intValue()    )),
                            convert.valueOf(   (int)(scale.h * h.intValue()    )),
                            convert
                        );
                } else if (w instanceof Byte) {
                    return new Dimension<T>(
                            convert.valueOf(   (byte)(scale.w * w.byteValue()   )),
                            convert.valueOf(   (byte)(scale.h * h.byteValue()   )),
                            convert
                        );
                } else if (w instanceof Double) {
                    return new Dimension<T>(
                            convert.valueOf(   (double)(scale.w * w.doubleValue() )),
                            convert.valueOf(   (double)(scale.h * h.doubleValue() )),
                            convert
                        );
                } else if (w instanceof Float) {
                    return new Dimension<T>(
                            convert.valueOf(   (float)(scale.w * w.floatValue()  )),
                            convert.valueOf(   (float)(scale.h * h.floatValue()  )),
                            convert
                        );
                } else if (w instanceof Short) {
                    return new Dimension<T>(
                            convert.valueOf(   (short)(scale.w * w.shortValue()  )),
                            convert.valueOf(   (short)(scale.h * h.shortValue()  )),
                            convert
                        );
                } else if (w instanceof Long) {
                    return new Dimension<T>(
                            convert.valueOf(   (long)(scale.w * w.longValue()   )),
                            convert.valueOf(   (long)(scale.h * h.longValue()   )),
                            convert
                        );
                } else {
                    throw new UnsupportedOperationException();
                }
            }
        }
        @Override
        public String toString() {
            return "Dimension(" + this.w + "x" + this.h + ")";
        }
    }
    
    public static final class Rectangle<T extends Number> {
        final Point<T> p;
        final Dimension<T> d;
        final public T x;
        final public T y;
        final public T w;
        final public T h;
        
        Rectangle(final Point<T> p, final Dimension<T>d) {
            this.p = p;
            this.d = d;
            this.x = p.x;
            this.y = p.y;
            this.w = d.w;
            this.h = d.h;
        }
        public Rectangle(final T x, final T y, final T w, final T h, final Converter<T> convert) {
            this.p = new Point<T>(x,y, convert);
            this.d = new Dimension<T>(w,h, convert);
            this.x = p.x;
            this.y = p.y;
            this.w = d.w;
            this.h = d.h;
        }
    }

    
}
