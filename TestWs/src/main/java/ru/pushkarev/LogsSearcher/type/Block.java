package ru.pushkarev.LogsSearcher.type;

import javax.validation.constraints.NotNull;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


public class Block {
    private final int start;
    private final int end;

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public Block(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public static Set<Block> getBlocksToRead(@NotNull List<Integer> hitList , @NotNull List<Integer> blockList) {
        Set<Block> resultBlocks = new LinkedHashSet<>();

        for (int i = 0, j_last = 0; i < hitList.size(); i++) {  // get line number
            for (int j = j_last; j < blockList.size() ; j++) { // iterate trough all blocks
                if(j == blockList.size() - 1 && hitList.get(i) >= blockList.get(j)) {  // handle last block
                    resultBlocks.add(new Block(blockList.get(j), Integer.MAX_VALUE));
                    j_last = j;
                    break;
                }
                // hit => block start          and         hit <= next block start
                if (hitList.get(i) >= blockList.get(j) && hitList.get(i) < blockList.get(j + 1)) { // next hit in another block
                    // check if next hit in another block?
                    resultBlocks.add(new Block(blockList.get(j), blockList.get(j+1) -1));
                    j_last = j;
                    break;
                }
            }
        }
        return resultBlocks;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + start;
        result = prime * result + end;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        Block other = (Block) obj;
        if (start != other.getStart())
            return false;
        return end == other.getEnd();
    }
}
