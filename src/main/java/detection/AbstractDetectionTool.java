package detection;

import models.PatternInstance;

import java.util.Set;

public abstract class AbstractDetectionTool {

    public abstract Set<PatternInstance> scanForPatterns();
}
