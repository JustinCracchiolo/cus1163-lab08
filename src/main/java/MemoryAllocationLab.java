import java.io.*;
import java.util.*;

public class MemoryAllocationLab {

    static class MemoryBlock {
        int start;
        int size;
        String processName;  // null if free

        public MemoryBlock(int start, int size, String processName) {
            this.start = start;
            this.size = size;
            this.processName = processName;
        }

        public boolean isFree() {
            return processName == null;
        }

        public int getEnd() {
            return start + size - 1;
        }
    }

    static int totalMemory;
    static ArrayList<MemoryBlock> memory;
    static int successfulAllocations = 0;
    static int failedAllocations = 0;

    /**
     * 
     * <p>
     * This method reads the input file and processes each REQUEST and RELEASE.
     * <p>
     * Read and parse the file
     *   - Open the file using BufferedReader
     *   - Read the first line to get total memory size
     *   - Initialize the memory list with one large free block
     *   - Read each subsequent line and parse it
     *   - Call appropriate method based on REQUEST or RELEASE
     * <p>
     * Implement allocation and deallocation
     *   - For REQUEST: implement First-Fit algorithm
     *     * Search memory list for first free block >= requested size
     *     * If found: split the block if necessary and mark as allocated
     *     * If not found: increment failedAllocations
     *   - For RELEASE: find the process's block and mark it as free
     *   - Optionally: merge adjacent free blocks (bonus)
     */
    public static void processRequests(String filename) {
        memory = new ArrayList<>();

        // Read file and initialize memory
        // Try-catch block to handle file reading
        // Read first line for total memory size
        // Create initial free block: new MemoryBlock(0, totalMemory, null)
        // Read remaining lines in a loop
        // Parse each line and call allocate() or deallocate()
        try {
             BufferedReader br = new BufferedReader(new FileReader(filename));
             int totalMemory = Integer.parseInt( br.readLine());
             memory.add(new MemoryBlock(0, totalMemory,null));
             
             String currentLine;
             while((currentLine = br.readLine()) != null) {
                 currentLine.trim();
                 String[] work = currentLine.split("\\s+"); 
                 /* 
                  * split the todo 
                  * index 0:request
                  * index 1 name 
                  * index 2 size
                  * or 
                  * index 0: release
                  * index 1 name 
                 */ 
                 if(work[0].equals("REQUEST")) {
                   String name = work[1];
                   int size = Integer.parseInt(work[2]);
                   allocate(name, size);
                 } else {
                     deallocate(work[1]);
                 }
             }
        
        } catch(IOException e) {
           System.out.println("Error reading file " + e.getMessage());
        }   
        
        
    }

    /**
     * Allocate memory using First-Fit
     */
     private static void allocate(String processName, int size) {
        // Search through memory list
        // Find first free block where size >= requested size
        // If found:
        //   - Mark block as allocated (set processName)
        //   - If block is larger than needed, split it:
        //     * Create new free block for remaining space
        //     * Add it to memory list after current block
        //   - Increment successfulAllocations
        //   - Print success message
        // If not found:
        //   - Increment failedAllocations
        //   - Print failure message
        for(int i = 0; i < memory.size(); i++) {
             MemoryBlock mb = memory.get(i);
             int originalSize = mb.size;
             if(mb.isFree() && mb.size >= size) {
                MemoryBlock allocatedMB = new MemoryBlock(mb.start, size, processName);
                memory.set(i, allocatedMB);

                if(mb.size > size) {
                    MemoryBlock remainingBlock = new MemoryBlock(mb.start + size, originalSize - size, null);
                    memory.add(i + 1, remainingBlock);
                }
                successfulAllocations++;
                System.out.println("Reqeust " + processName + " of size " + size + "KB completed");
                return;
             }
        }
        failedAllocations++;
        System.out.println("Request " + processName + " of size " + size + " failed");
        return;
    }
   
     private static void deallocate(String processName) {
        for(int i = 0; i < memory.size() - 1; i++) {
            MemoryBlock currentMB = memory.get(i);
            if(!currentMB.isFree() && currentMB.processName.equals(processName)) {
               currentMB.processName = null;
               System.out.println("Release of " + processName + " completed");
            }
        }
        mergeAdjacentBlocks();
    }

    private static void mergeAdjacentBlocks() {
        for(int i = 0; i < memory.size() - 1; i++) {
             MemoryBlock current = memory.get(i);
             MemoryBlock next = memory.get(i + 1);
             if(current.isFree() && next.isFree()) {
                 current.size += next.size;
                 memory.remove(i+1);
                 i--;
             }
        }
    }

    public static void displayStatistics() {
        System.out.println("\n========================================");
        System.out.println("Final Memory State");
        System.out.println("========================================");

        int blockNum = 1;
        for (MemoryBlock block : memory) {
            String status = block.isFree() ? "FREE" : block.processName;
            String allocated = block.isFree() ? "" : " - ALLOCATED";
            System.out.printf("Block %d: [%d-%d]%s%s (%d KB)%s\n",
                    blockNum++,
                    block.start,
                    block.getEnd(),
                    " ".repeat(Math.max(1, 10 - String.valueOf(block.getEnd()).length())),
                    status,
                    block.size,
                    allocated);
        }

        System.out.println("\n========================================");
        System.out.println("Memory Statistics");
        System.out.println("========================================");

        int allocatedMem = 0;
        int freeMem = 0;
        int numProcesses = 0;
        int numFreeBlocks = 0;
        int largestFree = 0;

        for (MemoryBlock block : memory) {
            if (block.isFree()) {
                freeMem += block.size;
                numFreeBlocks++;
                largestFree = Math.max(largestFree, block.size);
            } else {
                allocatedMem += block.size;
                numProcesses++;
            }
        }

        double allocatedPercent = (allocatedMem * 100.0) / totalMemory;
        double freePercent = (freeMem * 100.0) / totalMemory;
        double fragmentation = freeMem > 0 ?
                ((freeMem - largestFree) * 100.0) / freeMem : 0;

        System.out.printf("Total Memory:           %d KB\n", totalMemory);
        System.out.printf("Allocated Memory:       %d KB (%.2f%%)\n", allocatedMem, allocatedPercent);
        System.out.printf("Free Memory:            %d KB (%.2f%%)\n", freeMem, freePercent);
        System.out.printf("Number of Processes:    %d\n", numProcesses);
        System.out.printf("Number of Free Blocks:  %d\n", numFreeBlocks);
        System.out.printf("Largest Free Block:     %d KB\n", largestFree);
        System.out.printf("External Fragmentation: %.2f%%\n", fragmentation);

        System.out.println("\nSuccessful Allocations: " + successfulAllocations);
        System.out.println("Failed Allocations:     " + failedAllocations);
        System.out.println("========================================");
    }

    /**
     * Main method (FULLY PROVIDED)
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java MemoryAllocationLab <input_file>");
            System.out.println("Example: java MemoryAllocationLab memory_requests.txt");
            return;
        }

        System.out.println("========================================");
        System.out.println("Memory Allocation Simulator (First-Fit)");
        System.out.println("========================================\n");
        System.out.println("Reading from: " + args[0]);

        processRequests(args[0]);
        displayStatistics();
    }
}
