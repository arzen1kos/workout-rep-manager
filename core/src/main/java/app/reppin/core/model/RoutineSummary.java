package app.reppin.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** The routine summary panel (SPEC RTN-4): exercises, reps per set, total sets, estimated time. */
public final class RoutineSummary {

    /** One exercise block's line in the summary. */
    public static final class Line {
        private final String blockId;
        private final String exerciseId;
        private final int sets;
        private final int repEquivalentsPerSet;

        Line(String blockId, String exerciseId, int sets, int repEquivalentsPerSet) {
            this.blockId = blockId;
            this.exerciseId = exerciseId;
            this.sets = sets;
            this.repEquivalentsPerSet = repEquivalentsPerSet;
        }

        public String blockId() {
            return blockId;
        }

        public String exerciseId() {
            return exerciseId;
        }

        public int sets() {
            return sets;
        }

        public int repEquivalentsPerSet() {
            return repEquivalentsPerSet;
        }
    }

    private final List<Line> lines;
    private final int totalSets;
    private final long totalRepEquivalents;
    private final long estimatedMs;
    private final boolean estimateIncomplete;

    RoutineSummary(List<Line> lines, int totalSets, long totalRepEquivalents, long estimatedMs,
                   boolean estimateIncomplete) {
        this.lines = Collections.unmodifiableList(new ArrayList<>(lines));
        this.totalSets = totalSets;
        this.totalRepEquivalents = totalRepEquivalents;
        this.estimatedMs = estimatedMs;
        this.estimateIncomplete = estimateIncomplete;
    }

    public List<Line> lines() {
        return lines;
    }

    public int totalSets() {
        return totalSets;
    }

    public long totalRepEquivalents() {
        return totalRepEquivalents;
    }

    /** Estimated total time of the parts that can be estimated (see {@link #isEstimateIncomplete()}). */
    public long estimatedMs() {
        return estimatedMs;
    }

    /** True when some block (e.g. distance with no pace data yet) could not be estimated. */
    public boolean isEstimateIncomplete() {
        return estimateIncomplete;
    }
}
