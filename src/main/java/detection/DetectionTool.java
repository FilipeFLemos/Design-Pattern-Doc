package detection;

import models.PatternInstance;

import java.util.Set;

public interface DetectionTool {

    Set<PatternInstance> scanForPatterns();
}
