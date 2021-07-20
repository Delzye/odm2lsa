# odm2lsa
Convert CDISC ODM (Operational Data Model) files to LimeSurvey Archives

**ATTENTION** The converter is in a very basic state right now.
Answers are not converted at all, the output is only an lss file.

## Usage

### As a jar

There are three parameters:

1. Path to the ODM file
2. Form to convert
3. output path (optional)

### In another maven project

You can install this project as a dependency for another project, then import the *OdmConverter* and use its method *convert()*.

## Missing Features

1. Answers are not converted
2. Questions with DataTypes like Text, Datetime, etc. are not converted
3. Questions are not constructed into arrays or similar
4. Conditions are not converted
5. Properties like Min-/Max-values are not converted
