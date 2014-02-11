package sogara.hadoop.subgraph;

import java.util.*;
import java.io.IOException;
import java.lang.InterruptedException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class CalculateNodeDegreeJob {
    public static float DENSITY = 0;

    public static class CalculateNodeDegreeMapper extends Mapper<Text, Text, IntWritable, IntWritable> {
        public void map(Text key, Text value, Context context) throws IOException, InterruptedException {
            CalculateNodeDegreeJob.DENSITY = Float.parseFloat(key.toString());
            String[] edge = value.toString().split(";");

            IntWritable node0 = new IntWritable(Integer.parseInt(edge[0]));
            IntWritable node1 = new IntWritable(Integer.parseInt(edge[1]));
            context.write(node0, node1);
            context.write(node1, node0);
        }
    }

    public static class CalculateNodeDegreeReducer extends Reducer<IntWritable, IntWritable, IntWritable, FloatWritable> {
        public void reduce(IntWritable key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
            int degree = 0;

            for (IntWritable val : values) {
            	degree++;
            }

            FloatWritable ratio = new FloatWritable(0);
            if (CalculateNodeDegreeJob.DENSITY > 0) {
                ratio.set((float) (degree / CalculateNodeDegreeJob.DENSITY));
            }
            context.write(key, ratio);
      }
    }

    public static Job createJob() throws IOException {
    	Job job = Job.getInstance();

        job.setMapOutputKeyClass(IntWritable.class);
        job.setMapOutputValueClass(IntWritable.class);
        job.setOutputKeyClass(IntWritable.class);
        job.setOutputValueClass(FloatWritable.class);

        job.setMapperClass(CalculateNodeDegreeMapper.class);
        job.setReducerClass(CalculateNodeDegreeReducer.class);

        job.setInputFormatClass(CustomInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        return job;
    }
}