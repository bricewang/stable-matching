#include <fstream>
#include <iostream>
#include <queue>
#include <sstream>
#include <string>
using namespace std;

/**
 * Currently, this algorithm can only do one-to-one matching.
 *
 * Input: suitors.txt and reviewers.txt
 * The first line of each is n, the number of suitors/reviewers.
 * The next n lines are space-separated lists.
 * Line i contains the preference list, in order of decreasing preference, of suitor/reviewer i.
 * Each list need not contain every choice; an individual who does not list a member
 * of the opposite group will not be matched with that member under any circumstances.
 *
 * NOTE: Uses functions from C++11, compile with "g++ -std=c++11 alg1.cpp -o [outfilename]"
*/

int main() {
        ifstream infile1("suitors.txt");
        ifstream infile2("reviewers.txt");
        string line;
        getline(infile1, line);
        int nSuits = stoi(line);
        queue<int> sPrefs[nSuits];
        for (int i = 0; i < nSuits; i++) {
                getline(infile1, line);
                stringstream prefstream;
                prefstream << line;
                string temp;
                while (getline(prefstream, temp, ' ')) {
                        sPrefs[i].push(stoi(temp));
                }
        }

        getline(infile2, line);
        int nRevs = stoi(line);
        int rPrefs[nRevs][nSuits] = {};
        for (int i = 0; i < nRevs; i++) {
                getline(infile2, line);
                stringstream prefstream;
                prefstream << line;
                string temp;
                for (int j = 0; j < nSuits && getline(prefstream, temp, ' '); j++) {
                        rPrefs[i][stoi(temp) - 1] = nSuits - j;
                }
        }

        queue<int> single;
        for (int i = 0; i < nSuits; i++) {
                single.push(i + 1);
        }
        
        int match[nRevs] = {};

        while (!single.empty()) {
                int s = single.front() - 1;
                int r = sPrefs[s].front() - 1;
                sPrefs[s].pop();
                if (match[r] == 0) {
                        match[r] = s + 1;
                        single.pop();
                        // cout << "new match: " << match[r] << ' ' << r + 1 << endl;
                } else if (rPrefs[r][s] > rPrefs[r][match[r] - 1]) {
                        single.pop();
                        single.push(match[r]);
                        match[r] = s + 1;
                        // cout << "switch: " << match[r] << ' ' << r + 1 << endl;
                }
        }

        for (int i = 0; i < nRevs; i++) {
                cout << match[i] << ' ' << i + 1 << endl;
        }
}                                                                                                                                                     1,1           Top
