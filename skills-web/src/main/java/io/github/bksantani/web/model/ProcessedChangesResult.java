package io.github.bksantani.web.model;

import java.util.List;

public record ProcessedChangesResult(List<DiffBlock> diffs, List<String> warnings){
}
