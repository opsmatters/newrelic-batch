![opsmatters](https://i.imgur.com/VoLABc1.png)

# New Relic Batch 
[![Build Status](https://travis-ci.org/opsmatters/newrelic-batch.svg?branch=master)](https://travis-ci.org/opsmatters/newrelic-batch)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.opsmatters/newrelic-batch/badge.svg?style=blue)](https://maven-badges.herokuapp.com/maven-central/com.opsmatters/newrelic-batch)
[![Javadocs](http://javadoc.io/badge/com.opsmatters/newrelic-batch.svg)](http://javadoc.io/doc/com.opsmatters/newrelic-batch)

Java library that allows New Relic objects to be created automatically from file based definitions.
New Relic Insights dashboards can be created from definition files in YAML format.
New Relic Alerts can be configured from alert policy definitions in CSV or spreadsheet format.
It provides a set of tools to accelerate or automate the deployment of New Relic Alerts and Insights dashboards.

## Examples

### Dashboards
First create a dashboard configuration to hold the dashboards:
```
DashboardConfiguration config = new DashboardConfiguration();
```
Next, load a file containing dashboards in YAML format into the dashboard configuration:
```
Reader reader = new FileReader("path/dashboards.yml");
config.setDashboards(DashboardParser.parseYaml(reader));
reader.close();
```
To carry out operations on the dashboards in the dashboard configuration, first create a manager:
```
DashboardManager manager = new DashboardManager("YOUR_API_KEY");
```
To create the dashboards from the dashboard configuration in New Relic:
```
List<Dashboard> created = manager.createDashboards(config.getDashboards());
```
Alternatively, to delete the dashboards in the dashboard configuration from New Relic:
```
List<Dashboard> deleted = manager.deleteDashboards(config.getDashboards());
```
Finally, to output a set of dashboards to a YAML file:
```
Writer writer = new FileWriter("path/new_dashboards.yml");
DashboardRenderer.toYaml(dashboards, writer);
writer.close();
```
Similarly, to output with a banner:
```
Writer writer = new FileWriter("path/new_dashboards.yml");
DashboardRenderer.builder().withBanner(true).title(OUTPUT_FILENAME).build().renderYaml(dashboards, writer);
writer.close();
```
An example YAML file containing multiple dashboards and widgets can be found in the [tests](src/test/resources/test-dashboards.yml).

### Alerts
First create an alert configuration to hold the alert channels, policies and conditions:
```
AlertConfiguration config = new AlertConfiguration();
```
Next, to load a file containing email alert channels into the alert configuration, first create a reader and then pass it to the appropriate parser:
```
InputStream is = new FileInputStream("path/alerts.xlsx");
InputFileReader reader = InputFileReader.builder()
    .name("alerts.xlsx")
    .worksheet("Email Channels")
    .withInputStream(is)
    .build();
config.setAlertChannels(EmailChannelParser.parse(reader));
is.close();
```
**Note** that alert channels, policies or conditions can be imported or exported using files in either CSV, XLS or XLSX format, where the file format is derived from the extension of the filename (either .csv, .xls or .xlsx).
The "worksheet" parameter defines the name of the sheet containing the required data if the file is a workbook in either XLS or XLSX format.

The following parsers have been included for alert channels:
* EmailChannelParser
* SlackChannelParser
* HipChatChannelParser
* OpsGenieChannelParser
* PagerDutyChannelParser
* CampfireChannelParser
* UserChannelParser
* VictorOpsChannelParser
* xMattersChannelParser

Similarly, to load a file containing alert policies into the alert configuration:
```
InputStream is = new FileInputStream("path/alerts.xlsx");
InputFileReader reader = InputFileReader.builder()
    .name("alerts.xlsx")
    .worksheet("Alert Policies")
    .withInputStream(is)
    .build();
config.setAlertPolicies(AlertPolicyParser.parse(reader));
is.close();
```
To carry out operations on the alert channels, policies or conditions in the alert configuration, first create a manager:
```
AlertManager manager = new AlertManager("YOUR_API_KEY");
```
To create the alert channels or policies from the alert configuration in New Relic:
```
List<AlertChannel> createdChannels = manager.createAlertChannels(config.getAlertChannels());
List<AlertPolicy> createdPolicies = manager.createAlertPolicies(config.getAlertPolicies());
```
Alternatively, to delete the alert channels or policies in the alert configuration from New Relic:
```
List<AlertChannel> deletedChannels = manager.deleteAlertChannels(config.getAlertChannels());
List<AlertPolicy> deletedPolicies = manager.deleteAlertPolicies(config.getAlertPolicies());
```
Finally, to output a set of email alert channels to a spreadsheet file, first create a writer and then pass it to the appropriate renderer:
```
OutputStream os = new FileOutputStream("path/new-alerts.xlsx");
OutputFileWriter writer = OutputFileWriter.builder()
    .name("new-alerts.xlsx")
    .worksheet("Alert Channels")
    .withOutputStream(os)
    .build();
EmailChannelRenderer.write(policies, writer);
os.close();
writer.close();
```
Next, to add a set of alert policies as a new sheet in the same workbook, first get the existing workbook and then pass it to the writer:
```
Workbook workbook = Workbook.getWorkbook(new File("path", "new-alerts.xlsx"));
OutputStream os = new FileOutputStream("path/new-alerts.xlsx");
OutputFileWriter writer = OutputFileWriter.builder()
    .name("new-alerts.xlsx")
    .worksheet("Alert Policies")
    .withOutputStream(os)
    .withWorkbook(workbook)
    .build();
AlertPolicyRenderer.write(policies, writer);
os.close();
writer.close();
```
Renderers have been included to complement every parser.

An example spreadsheet file containing multiple alert policies can be found in the [tests](src/test/resources/test-alerts.xlsx).

## Prerequisites

A New Relic account with an Admin user.
The user needs to generate an [Admin API Key](https://docs.newrelic.com/docs/apis/rest-api-v2/getting-started/api-keys) 
to provide read-write access via the [New Relic REST APIs](https://api.newrelic.com).
The Admin API Key is referenced in the documentation as the parameter "YOUR_API_KEY".

## Installing

First clone the repository using:
```
>$ git clone https://github.com/opsmatters/newrelic-batch.git
>$ cd newrelic-batch
```

To compile the source code, run all tests, and generate all artefacts (including sources, javadoc, etc):
```
mvn package 
```

## Running the tests

To execute the unit tests:
```
mvn clean test 
```

The following tests are included:

* NewRelicDashboardTest: Reads the definition of several dashboards containing multiple widgets from a YAML file and creates the dashboards in New Relic. Then exports the same dashboards to a different YAML file.
* NewRelicAlertTest: Reads the definition of several alert policies from a spreadsheet file and creates the policies in New Relic. Then exports the same policies to a different spreadsheet file.

## Deployment

The build artefacts are hosted in The Maven Central Repository. 

Add the following dependency to include the artefact within your project:
```
<dependency>
  <groupId>com.opsmatters</groupId>
  <artifactId>newrelic-batch</artifactId>
  <version>0.3.0</version>
</dependency>
```

## Built With

* [newrelic-api](https://github.com/opsmatters/newrelic-api) - Java client library for the New Relic Monitoring and Alerting REST APIs
* [opsmatters-core](https://github.com/opsmatters/opsmatters-core) - Core library for the opsmatters suite
* [SnakeYAML](https://bitbucket.org/asomov/snakeyaml) - A YAML processor for the Java Virtual Machine
* [Maven](https://maven.apache.org/) - Dependency Management
* [JUnit](http://junit.org/) - Unit testing framework

## Contributing

Please read [CONTRIBUTING.md](https://www.contributor-covenant.org/version/1/4/code-of-conduct.html) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning

This project use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/opsmatters/newrelic-batch/tags). 

## Authors

* **Gerald Curley** - *Initial work* - [opsmatters](https://github.com/opsmatters)

See also the list of [contributors](https://github.com/opsmatters/newrelic-batch/contributors) who participated in this project.

## License

This project is licensed under the terms of the [Apache license 2.0](https://www.apache.org/licenses/LICENSE-2.0.html).

<sub>Copyright (c) 2018 opsmatters</sub>