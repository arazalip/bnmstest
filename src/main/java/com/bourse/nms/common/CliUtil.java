package com.bourse.nms.common;

import org.apache.commons.cli.*;
import org.apache.log4j.PropertyConfigurator;

/**
 * Created by IntelliJ IDEA.
 * User: araz
 * Date: 5/19/12
 * Time: 8:48 PM
 */
public class CliUtil {
    public static final OptionGroup LOG4J_OPTION_GROUP = new OptionGroup();

    private final static Options options;

    static {
        options = new Options();
        /*
        options.addOption("l", "log", true, "Log4j Config file path");
        options.addOption("d", "delay", true, "Log4j reload delay (in seconds)");
        */

        final Option pathOption = new Option("l", "log", true, "Log4j Config file path");
        final Option delayOption = new Option("d", "delay", true, "Log4j reload delay (in seconds)");

        LOG4J_OPTION_GROUP.addOption(pathOption);
        LOG4J_OPTION_GROUP.addOption(delayOption);

        options.addOption(pathOption);
        options.addOption(delayOption);
    }

    public static Options getOptions() {
        return options;
    }

    public static CommandLine parsArgument(String[] args) {

        final CommandLineParser parser = new GnuParser();
        final CommandLine line;

        try {
            // parse the command line arguments
            line = parser.parse(options, args);
        } catch (ParseException exp) {
            // oops, something went wrong
            System.out.println("Argument Parsing failed.  Reason: " + exp.getMessage());
            System.exit(-1);
            return null;
        }

        parsLine(line);
        return line;

    }

    public static void parsLine(final CommandLine line) {

        if (line.hasOption("log")) {
            final String logConfigPath = line.getOptionValue("log");
            System.out.println("Loading log config from path: " + logConfigPath);

            if (line.hasOption("delay")) {
                final String delayStr = line.getOptionValue("delay");
                final long delay;
                try {
                    delay = Long.parseLong(delayStr);
                } catch (NumberFormatException nfe) {
                    System.err.println("Exception in parsing delay. it should be a valid long: " + delayStr);
                    System.exit(-1);
                    return;
                }

                System.out.println("Reloading log config in delay (sec): " + delay);

                PropertyConfigurator.configureAndWatch(logConfigPath, delay * 1000);
            } else {
                PropertyConfigurator.configureAndWatch(logConfigPath);
            }
        }
    }

}
