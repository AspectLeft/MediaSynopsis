package app.model;

import java.util.List;

public abstract class SynopsisGeneratorBase{
    protected static final int W0 = 352, H0 = 288;

    public abstract Synopsis generate(final List<Media> mediaList);
}
