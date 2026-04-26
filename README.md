# GDPR-Compliance-Plugin

![Build](https://github.com/KevinDunne2003/GDPR-Compliance-Plugin/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)

<!-- Plugin description -->
## Running and Testing the Plugin

This project must be opened in IntelliJ IDEA.

Open the plugin project in IntelliJ IDEA as a normal Gradle-based project. Once IntelliJ has finished loading the project and Gradle has finished syncing, open a terminal inside IntelliJ IDEA.

The terminal can be opened from inside IntelliJ IDEA using the built-in terminal window.

From the terminal inside IntelliJ IDEA, run the following command:

```bash
./gradlew runIde
```

This command starts a new development instance of IntelliJ IDEA.

The development instance is a separate IntelliJ IDEA window that runs with this plugin installed. This allows the plugin to be tested without installing it into your main IntelliJ IDEA environment.

---

## Opening a Test Project

Once the development instance starts, a new IntelliJ IDEA window will appear.

You will be prompted with a window asking you to open a project.

To test the plugin under the same parameters used during this project, download the FenixEdu Academic project from the following GitHub repository:

https://github.com/FenixEdu/fenixedu-academic

After downloading the FenixEdu Academic project, open it inside the development IntelliJ IDEA instance.

The plugin should be run against this project, not against the plugin project itself.

---

## Running the GDPR Compliance Plugin

After the FenixEdu Academic project has been opened in the development IntelliJ IDEA instance, run the plugin from the IntelliJ IDEA menu.

To do this:

1. Click the main menu option at the top-left of the IDE.
2. Click **Tools**.
3. Click **Run GDPR Compliance Plugin**.

This will start the plugin.

---

## Selecting the Data Catalogue

After clicking **Run GDPR Compliance Plugin**, a window will appear asking for the location of the data catalogue on your local file system.

You must provide the local file system path to the data catalogue.

The data catalogue file must be named exactly:

```text
DataCatalogue.xlsx
```

The plugin expects this exact file name. If the file is named differently, the plugin may not run correctly.

---

## Running the Analysis

Once the data catalogue has been selected, the plugin will begin running against the project that is currently open in the development IntelliJ IDEA instance.

The time taken for the plugin to complete depends on:

- the size of the project being analysed
- the size of the data catalogue
- the number of code elements scanned
- the number of possible matches and violations detected

Larger systems and larger data catalogues may take longer to analyse. During the testing involved in this project, the analysis took approximately 20 minutes to run.

---

## Exported Results

When the plugin has finished running, the results are exported to the following directory:

```text
src/main/resources/reports
```

## Final Remarks

Inside startup/ReportGenerators/ViolationReportExporter, there is a path to where the results will be exported to. Please change this to the location on your local machine you would like the report to be exported to
 
<!-- Plugin description end -->

## Installation

- Using the IDE built-in plugin system:

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "GDPR-Compliance-Plugin"</kbd> >
  <kbd>Install</kbd>

- Using JetBrains Marketplace:

  Go to [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID) and install it by clicking the <kbd>Install to ...</kbd> button in case your IDE is running.

  You can also download the [latest release](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID/versions) from JetBrains Marketplace and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

- Manually:

  Download the [latest release](https://github.com/KevinDunne2003/GDPR-Compliance-Plugin/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>


---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
[docs:plugin-description]: https://plugins.jetbrains.com/docs/intellij/plugin-user-experience.html#plugin-description-and-presentation