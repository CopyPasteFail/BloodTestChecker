# Blood Test Checker
Check your Cholesterol and A1C levels on your blood test

## Table of content
- [Functionality](#Functionality)
- [Installation](#Installation)
- [Maintainers](#Maintainers)
- [Contributing](#Contributing)
- [License](#license)
- [Links](#links)


## Functionality
The app allows the user to submit data which simulates blood test result
in the following structure:
Test name - String in free text (e.g. "Total HDL Cholesterol")
Test result value - Number (e.g. 40)

Upon submission of the data, the app will:
Analyze the provided data using external dataset (see "Dataset" below)
Identify the category of the test by parsing the Test name's free text
Evaluate the result value based on the test category and the result threshold
Inform the user whether their result is "Good!" (i.e. below threshold),
"Bad!" (i.e. above threshold) or “Unknown” (i.e test not found in dataset)

### Dataset
The dataset can be found in: 

### Important implementation notes
The user input can be within the following character set:
'A-Z', 'a-z', '0-9' and '(),-:/!'
User errors of test names can be forgiven  by giving a little leeway for different word ordering, punctuation and typos

### Examples
User input - "Cholesterol - HDL" with the value 39. Output: "HDL Cholesterol" and "Good!"
User input - "HDL, Total" with the value 50. Output: "HDL Cholesterol" and "Bad!"
User input - "CHOLESTEROL-LDL calc" with the value 99. Output: "HDL Cholesterol" and "Good!"
User input - "HM Hemoglobin - A1C" with the value 12. Output: "A1C" and "Bad!"
User input - "Triglycerides" with the value 120. Output: "Unknown"



## Installation
Clone this repository and import into **Android Studio**
```bash
git clone https://github.com/GipsyBeggar/BloodTestChecker.git
```


## Maintainers
This project is mantained by:
* [Omer Reznik](http://github.com/GipsyBeggar)


## Contributing
1. Fork it
2. Create your feature branch (git checkout -b my-new-feature)
3. Commit your changes (git commit -m 'Add some feature')
4. Push your branch (git push origin my-new-feature)
5. Create a new Pull Request


## License
This project is licensed under the GNU Affero General Public License v3.0 - see the [LICENSE.md](LICENSE.md) file for details
