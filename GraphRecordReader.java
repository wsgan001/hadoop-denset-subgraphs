package sogara.hadoop.subgraph;

import java.io.IOException;
import java.lang.InterruptedException;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.util.LineReader;

public class GraphRecordReader extends RecordReader<IntWritable, IntWritable> {
    private LineReader in;
    private IntWritable key;
    private IntWritable value;
    private long start = 0;
    private long end = 0;
    private long pos = 0;
 
    @Override
    public void close() throws IOException {
        if (in != null) {
            in.close();
        }
    }
 
    @Override
    public IntWritable getCurrentKey() throws IOException {
        return key;
    }
 
    @Override
    public IntWritable getCurrentValue() throws IOException {
        return value;
    }
 
    @Override
    public float getProgress() throws IOException{
        if (start == end) {
            return 0.0f;
        }
        else {
            return Math.min(1.0f, (pos - start) / (float)(end - start));
        }
    }
 
    @Override
    public void initialize(InputSplit genericSplit, TaskAttemptContext context) throws IOException, InterruptedException {
        FileSplit split = (FileSplit) genericSplit;
        final Path file = split.getPath();
        Configuration conf = context.getConfiguration();
        FileSystem fs = file.getFileSystem(conf);
        start = split.getStart();
        end = start + split.getLength();
        boolean skipFirstLine = false;
        FSDataInputStream filein = fs.open(split.getPath());
 
        if (start != 0){
            skipFirstLine = true;
            --start;
            filein.seek(start);
        }
        in = new LineReader(filein, conf);
        if (skipFirstLine) {
            start += in.readLine(new Text(), 0, (int)Math.min((long)Integer.MAX_VALUE, end - start));
        }
        this.pos = start;
    }
 
    @Override
    public boolean nextKeyValue() throws IOException, InterruptedException {
        if (key == null) {
	        key = new IntWritable();
        }
		if (value == null) {
        	value = new IntWritable();
		}
		Text edge = new Text();
		int newSize = 0;
        newSize = in.readLine(edge, Integer.MAX_VALUE, (int)Math.min((long)Integer.MAX_VALUE, end - pos));

        if (newSize == 0) {
            key = null;
            value = null;
            return false;
        } else {
        	String[] edgeArray = edge.toString().split(" ");
        	key.set(Integer.parseInt(edgeArray[0]));
        	value.set(Integer.parseInt(edgeArray[1]));
        	pos += newSize;
            return true;
        }
    }
}