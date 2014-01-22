package com.indeed.util.urlparsing.benchmark;

import com.google.common.base.Stopwatch;
import com.google.common.io.CharSource;
import com.google.common.io.CharStreams;
import com.google.common.io.LineProcessor;
import com.google.common.io.Resources;
import com.indeed.util.urlparsing.ParseUtils;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author: preetha
 */
public class NumberParsingBenchmark {

    private static final NumberParser indeedNumberParser = new NumberParser() {
        @Override
        public float parseFloat(String line, int start, int end) {
            return ParseUtils.parseFloat(line, start, end);
        }

        @Override
        public int parseInt(String line, int start, int end) {
            return  ParseUtils.parseUnsignedInt(line, start, end);
        }
    };

    private static final NumberParser javaNumberParser = new NumberParser() {
        @Override
        public float parseFloat(String line, int start, int end) {
            String num = line.substring(start, end);
            return Float.parseFloat(num);
        }

        @Override
        public int parseInt(String line, int start, int end) {
            String num = line.substring(start, end);
            return Integer.parseInt(num);
        }
    }  ;

    public static void main(String[] args) throws IOException {

        final NumberParser numParser;
        if (args.length> 0 &&  "ind".equals(args[0]) ) {
            numParser = indeedNumberParser;
        } else {
           numParser = javaNumberParser;
        }

        final CharSource intData = Resources.asCharSource(Resources.getResource("text_with_ints.txt"), Charset.forName("UTF-8"));
        final CharSource floatData = Resources.asCharSource(Resources.getResource("text_with_floats.txt"), Charset.forName("UTF-8"));

        final Stopwatch stopwatchA = Stopwatch.createUnstarted();

        final Stopwatch stopwatchB = Stopwatch.createUnstarted();

        System.out.println("parsing utils results");

        doNumberParsingBenchMark(intData, floatData, stopwatchA, stopwatchB, numParser);



    }

    public static void doNumberParsingBenchMark(final CharSource intData, final CharSource floatData, final Stopwatch stopwatchA, final Stopwatch stopwatchB, final NumberParser numberParser) throws IOException {

        final Integer numInts = CharStreams.readLines(intData, new LineProcessor<Integer>() {
            private int count;

            @Override
            public boolean processLine(String line) throws IOException {
                count++;
                int endPos = line.indexOf(",");
                stopwatchA.start();
                numberParser.parseInt(line, 0, endPos);
                stopwatchA.stop();
                return true;
            }

            @Override
            public Integer getResult() {
                return count;
            }
        });

        final Integer numFloats = CharStreams.readLines(floatData, new LineProcessor<Integer>() {
            private int count;

            @Override
            public boolean processLine(String line) throws IOException {
                count++;
                int endPos = line.indexOf(",");
                stopwatchB.start();
                numberParser.parseFloat(line, 0, endPos);
                stopwatchB.stop();
                return true;
            }

            @Override
            public Integer getResult() {
                return count;
            }
        });


        System.out.println("# of ints = " + numInts + " parseInt time " + stopwatchA);
        System.out.println("# of floats = " + numFloats + " parseFloat time " + stopwatchB);
    }


}
