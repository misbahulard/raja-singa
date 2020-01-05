package com.misbahulard;

import com.misbahulard.model.*;
import com.misbahulard.util.ApiClient;
import com.misbahulard.util.ApiService;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;

import java.io.*;
import java.sql.*;
import java.util.*;

import static java.lang.System.exit;

public class App {

    // JDBC driver name and database URL
    private static final String JDBC_DRIVER = "org.h2.Driver";
    private static String h2Url, h2User, h2Pass;
    private static final Logger logger = LoggerFactory.getLogger(App.class);
    private static String token;
    private static String phpipamUsername;
    private static String phpipamPassword;
    private static String phpipamDev, phpipamQa, phpipamNpUtility, phpipamProd, phpipamDr;
    private static String domainDev1, domainDev2, domainQa1, domainQa2, domainNpUtility, domainProd, domainDr;
    private static Boolean force = false;

    public static void main(String[] args) {
        showBanner();

        Options options = new Options();

        Option userOption = new Option("user", true, "linux system user");
        userOption.setRequired(true);
        options.addOption(userOption);

        Option envOption = new Option("env", true, "environment (dev, qa, np_utility, prod, dr)");
        envOption.setRequired(true);
        options.addOption(envOption);

        Option domainOption = new Option("domain", true, "domain name, if set: it will be appended to hostname list in csv");
        options.addOption(domainOption);

        Option csvOption = new Option("csvpath", true, "csv file path");
        options.addOption(csvOption);

        Option portOption = new Option("port", true, "ssh port");
        options.addOption(portOption);

        Option authorizeKeyPathOption = new Option("authkeypath", true, "authorized_keys path");
        options.addOption(authorizeKeyPathOption);

        Option dbOption = new Option("db", false, "connect to h2 db and sync");
        options.addOption(dbOption);

        Option ipamOption = new Option("ipam", false, "fetch hosts from phpipam");
        options.addOption(ipamOption);

        Option forceOption = new Option("force", false, "force execution without confirmation");
        options.addOption(forceOption);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine commandLine = null;

        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("raja-singa", options);
            exit(1);
        }

        String env = commandLine.getOptionValue("env");
        String csvFile = commandLine.getOptionValue("csvpath");
        String user = commandLine.getOptionValue("user");
        String domain = commandLine.getOptionValue("domain");
        String port = commandLine.getOptionValue("port") != null ? commandLine.getOptionValue("port") : "22";
        String authorizeKeyPath = commandLine.getOptionValue("authkeypath") != null ? commandLine.getOptionValue("authkeypath") : "~/.ssh/authorized_keys";
        if (commandLine.hasOption("force")) {
            force = true;
        }

        // show info
        logger.info("--------[FLAG INFO]--------");
        logger.info("env: {}", env);
        logger.info("sync db: {}", commandLine.hasOption("db"));
        logger.info("fetch from ipam: {}", commandLine.hasOption("ipam"));
        logger.info("user: {}", user);
        logger.info("domain: {}", domain);
        logger.info("port: {}", port);
        logger.info("authorize key path: {}", authorizeKeyPath);
        logger.info("csv file path: {}", csvFile);
        logger.info("---------------------------");

        readProperties();

        if (commandLine.hasOption("ipam")) {
            List<SystemBastillion> systemBastillions = fetchFromIpam(commandLine.getOptionValue("env"), user);
            if (commandLine.hasOption("db")) {
                // sync with DB
                syncDB(systemBastillions, user, port, authorizeKeyPath, env);
            } else {
                // Write to sql file
                List<String> queries = generateQuery(systemBastillions, port, authorizeKeyPath);
                writeSql("ipam.sql", queries);
            }
        } else {
            String fileName;
            if (domain != null) {
                fileName = user + "-" + domain + ".sql";
            } else {
                fileName = user + ".sql";
            }

            // Read CSV
            List<SystemBastillion> systemBastillions = readCsv(user, domain, env, csvFile);

            if (commandLine.hasOption("db")) {
                syncDB(systemBastillions, user, port, authorizeKeyPath, env);
            } else {
                List<String> queries = generateQuery(systemBastillions, port, authorizeKeyPath);
                writeSql(fileName, queries);
            }
        }

        logger.info("have a nice day :)");
    }

    public static List<String> generateQuery(List<SystemBastillion> systemBastillions, String port, String authorizeKeyPath) {
        List<String> queries = new ArrayList<>();
        String query;

        logger.info("generate sql query");
        for (SystemBastillion sb : systemBastillions) {
            query = String.format("INSERT INTO SYSTEM (DISPLAY_NM, USER, HOST, PORT, AUTHORIZED_KEYS) VALUES ('%s', '%s', '%s', %s, '%s');", sb.getDisplayName(), sb.getUser(), sb.getHost(), port, authorizeKeyPath);
            queries.add(query);
        }
        return queries;
    }

    public static List<SystemBastillion> readCsv(String user, String domain, String env, String csvFile) {
        // Read CSV
        List<SystemBastillion> systemBastillions = new ArrayList<>();
        String host;
        String fqdn = "";
        String[] line;

        CSVReader reader = null;
        try {
            logger.info("read the csv file");
            reader = new CSVReader(new FileReader(csvFile));

            while ((line = reader.readNext()) != null) {
                host = line[0];
                if (!host.equals("")) {
                    if (domain != null) {
                        fqdn = host + "." + domain;
                    } else {
                        fqdn = host;
                    }
                }

                // for sync with DB
                SystemBastillion systemBastillion = new SystemBastillion();
                systemBastillion.setDisplayName(host + "-" + env);
                systemBastillion.setUser(user);
                systemBastillion.setHost(fqdn);
                systemBastillions.add(systemBastillion);
            }

            logger.info("total host in csv: {}", systemBastillions.size());
            return systemBastillions;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvValidationException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void writeSql(String fileName, List<String> queries) {
        logger.info("write to sql is selected");

        try {
            FileWriter fileWriter = new FileWriter(fileName);
            PrintWriter printWriter = new PrintWriter(fileWriter);

            for (String query : queries) {
                printWriter.println(query);
            }

            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("file successfully created in {}", fileName);
    }

    public static void syncDB(List<SystemBastillion> systemBastillionsCSV, String user, String port, String authorizeKeyPath, String env) {
        logger.info("sync with DB is selected");

        Connection conn = null;
        Statement stmt = null;

        try {
            Class.forName(JDBC_DRIVER);

            logger.info("connecting to database");
            conn = DriverManager.getConnection(h2Url, h2User, h2Pass);

            logger.info("fetch all system from DB");
            stmt = conn.createStatement();
            String sql = String.format("SELECT * FROM SYSTEM WHERE DISPLAY_NM LIKE '%%-%s' AND USER='%s'", env, user);
            ResultSet rs = stmt.executeQuery(sql);

            List<SystemBastillion> systemBastillionsDB = new ArrayList<>();

            while (rs.next()) {
                SystemBastillion sb = new SystemBastillion();
                sb.setHost(rs.getString("HOST"));
                sb.setUser(rs.getString("USER"));
                sb.setDisplayName("DISPLAY_NM");
                systemBastillionsDB.add(sb);
            }

            // get addition and substraction for hosts
            List<SystemBastillion> addition = getUnique(systemBastillionsCSV, systemBastillionsDB);
            logger.info("THERE IS {} NEW HOST WILL BE ADDED", addition.size());
            for (int i = 0; i < addition.size(); i++) {
                SystemBastillion sb = addition.get(i);
                logger.info("{}) {}, {}", i + 1, sb.getUser(), sb.getHost());
            }

            List<SystemBastillion> subtraction = getUnique(systemBastillionsDB, systemBastillionsCSV);
            logger.info("THE IS {} HOST WILL BE REMOVED", subtraction.size());
            for (int i = 0; i < subtraction.size(); i++) {
                SystemBastillion sb = subtraction.get(i);
                logger.info("{}) {}, {}", i + 1, sb.getUser(), sb.getHost());
            }

            if (addition.size() != 0 || subtraction.size() != 0) {
                if (!force) {
                    System.out.println("\n========================================================================");
                    System.out.println("AFTER THIS STEPS, THE SYSTEM WILL ADD AND DELETE THE HOST AUTOMATICALLY! \nARE YOU SURE [Y/N]?");
                    Scanner in = new Scanner(System.in);
                    String answer = in.nextLine();
                    System.out.println("\n========================================================================");

                    if (!answer.toLowerCase().equals("y")) {
                        logger.info("execution aborted!!!");
                        exit(0);
                    }
                }

                ArrayList<String> queries;
                // Insert if there are new systems
                if (addition.size() > 0) {
                    logger.info("generate insert query");
                    queries = new ArrayList<>();
                    for (SystemBastillion sb : addition) {
                        String query = String.format("INSERT INTO SYSTEM (DISPLAY_NM, USER, HOST, PORT, AUTHORIZED_KEYS) VALUES ('%s', '%s', '%s', %s, '%s');", sb.getDisplayName(), sb.getUser(), sb.getHost(), port, authorizeKeyPath);
                        queries.add(query);
                    }

                    logger.info("insert new hosts");
                    for (String query : queries) {
                        stmt = conn.createStatement();
                        stmt.execute(query);
                    }
                }

                if (subtraction.size() > 0) {
                    logger.info("generate delete query");
                    queries = new ArrayList<>();
                    for (SystemBastillion sb : subtraction) {
                        String query = String.format("DELETE FROM SYSTEM WHERE USER='%s' AND HOST='%s' AND DISPLAY_NM LIKE '%%%s';", sb.getUser(), sb.getHost(), env);
                        queries.add(query);
                    }

                    logger.info("delete hosts");
                    for (String query : queries) {
                        stmt = conn.createStatement();
                        stmt.execute(query);
                    }
                }

                logger.info("all clean, great");
            }

            stmt.close();
            conn.close();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException se2) {
            }
            try {
                if (conn != null) conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

    public static <T> List<T> getUnique(List<T> list1, List<T> list2) {
        List<T> result = new ArrayList<>();
        for (T obj : list1) {
            if (!list2.contains(obj)) {
                result.add(obj);
            }
        }
        return result;
    }

    public static void readProperties() {
        try {
            logger.info("read properties file config");
            InputStream inputStream = new FileInputStream("app.properties");
            Properties properties = new Properties();
            properties.load(inputStream);
            phpipamUsername = properties.getProperty("phpipam.username");
            phpipamPassword = properties.getProperty("phpipam.password");
            phpipamDev = properties.getProperty("phpipam.dev_id");
            phpipamQa = properties.getProperty("phpipam.qa_id");
            phpipamNpUtility = properties.getProperty("phpipam.np_utility_id");
            phpipamProd = properties.getProperty("phpipam.prod_id");
            phpipamDr = properties.getProperty("phpipam.dr_id");
            domainDev1 = properties.getProperty("domain.dev1");
            domainDev2 = properties.getProperty("domain.dev2");
            domainNpUtility = properties.getProperty("domain.np_utility");
            domainQa1 = properties.getProperty("domain.qa1");
            domainQa2 = properties.getProperty("domain.qa2");
            domainProd = properties.getProperty("domain.prod");
            domainDr = properties.getProperty("domain.dr");
            h2Url = properties.getProperty("h2.url");
            h2User = properties.getProperty("h2.user");
            h2Pass = properties.getProperty("h2.pass");
        } catch (FileNotFoundException e) {
            logger.error("properties file config not found!");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<SystemBastillion> fetchFromIpam(String env, String user) {
        List<SystemBastillion> systemBastillions = new ArrayList<>();
        ApiService apiServiceAuth = ApiClient.createService(ApiService.class, phpipamUsername, phpipamPassword);
        Call<AuthResponse> authService = apiServiceAuth.authService();
        String envId = null;

        logger.info("fetch from ipam selected");

        switch (env) {
            case "dev":
                envId = phpipamDev;
                break;
            case "qa":
                envId = phpipamQa;
                break;
            case "np_utility":
                envId = phpipamNpUtility;
                break;
            case "prod":
                envId = phpipamProd;
                break;
            case "dr":
                envId = phpipamDr;
                break;
            default:
                break;
        }

        logger.info("environment {} is selected", env.toUpperCase());
        try {
            logger.info("try to authenticate phpipam");
            token = authService.execute().body().getData().getToken();

            // Map headers
            Map<String, String> headers = new HashMap<>();
            headers.put("token", token);

            ApiService apiService = ApiClient.createService(ApiService.class);
            Call<SectionResponse> sectionService = apiService.sectionService(headers, envId);

            // get all subnets in sections
            logger.info("get all section from {} environment", env.toUpperCase());
            List<Section> sections = new ArrayList<>();
            sections.addAll(sectionService.execute().body().getData());

            // get all hosts in subnets
            for (Section section : sections) {
                logger.info("get all host from section {} {}", section.getId(), section.getDescription());
                Call<HostResponse> hostService = apiService.hostService(headers, section.getId());
                HostResponse hostResponse = hostService.execute().body();

                // get the right domain
                String domain = getDomainName(env, section.getDescription());

                // phpipam api fool! return true if success, but return 0 when it fail
                // so we just need compare message value, if not set is 'success'!!!
                if (hostResponse.getMessage() == null) {
                    for (Host host : hostResponse.getData()) {
                        // remove null, empty, and 'gateway' hostname
                        if (host.getHostname() != null && !host.getHostname().equals("") && !host.getHostname().equals("gateway")) {
                            // don't capture fqdn host!
                            if (!host.getHostname().matches(".*[.].*")) {
                                SystemBastillion sb = new SystemBastillion();
                                sb.setDisplayName(host.getHostname().toLowerCase() + "-" + env);
                                sb.setHost(host.getHostname().toLowerCase() + "." + domain);
                                sb.setUser(user);
                                systemBastillions.add(sb);
                            }
                        }
                    }
                }
            }
            logger.info("there is {} hosts", systemBastillions.size());
            logger.info("all host has been successfully obtained");

            return systemBastillions;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getDomainName(String env, String desc) {
        switch (env) {
            case "dev":
                if (desc.toLowerCase().equals("dev1"))
                    return domainDev1;
                else
                    return domainDev2;
            case "qa":
                if (desc.toLowerCase().equals("qa1"))
                    return domainQa1;
                else
                    return domainQa2;
            case "np_utility":
                return domainNpUtility;
            case "prod":
                return domainProd;
            case "dr":
                return domainDr;
            default:
                return null;
        }
    }

    public static void showBanner() {
        System.out.println("                                               ,w.\n" +
                "                                             ,YWMMw  ,M  ,\n" +
                "                        _.---.._   __..---._.'MMMMMw,wMWmW,\n" +
                "                   _.-\"\"        \"\"\"           YP\"WMMMMMMMMMb,\n" +
                "                .-' __.'                   .'     MMMMW^WMMMM;\n" +
                "    _,        .'.-'\"; `,       /`     .--\"\"      :MMM[==MWMW^;\n" +
                " ,mM^\"     ,-'.'   /   ;      ;      /   ,       MMMMb_wMW\"  @\\\n" +
                ",MM:.    .'.-'   .'     ;     `\\    ;     `,     MMMMMMMW `\"=./`-,\n" +
                "WMMm__,-'.'     /      _.\\      F\"\"\"-+,,   ;_,_.dMMMMMMMM[,_ / `=_}\n" +
                "\"^MP__.-'    ,-' _.--\"\"   `-,   ;       \\  ; ;MMMMMMMMMMW^``; __|\n" +
                "           /   .'            ; ;         )  )`{  \\ `\"^W^`,   \\  :\n" +
                "          /  .'             /  (       .'  /     Ww._     `.  `\"\n" +
                "         /  Y,              `,  `-,=,_{   ;      MMMP`\"\"-,  `-._.-,\n" +
                "        (--, )                `,_ / `) \\/\"\")      ^\"      `-, -;\"\\:\n" +
                "         `\"\"\"                    `\"\"\"   `\"'                  `---\"");
        System.out.println("                   RAJA SINGA [LION KING]\n");
    }
}
