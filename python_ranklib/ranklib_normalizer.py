'''
@Author Amith RC
@Created March 24th 2019
@Purpose: Creates the Ranklib feature file using the Run file (TREC car format)
'''
import argparse
import operator
import sys
import pandas as pd
import os
import numpy as np
import itertools
import subprocess as sp
from scipy.stats import zscore
import re

fetextract = re.compile(r'[0-9]+:[-.0-9]+')
verbose = False


def is_relevant(qrel, qid, pid):
    if qid in qrel:
        para = qrel.get(qid)
        if pid in para:
            return 1
        else:
            return 0
    else:
        return 0


'''
Write to the feature file format, write the feature file from the ranker dict
Append the qid_pid as info
We can use this to combine the rank files
'''


def write_feature_file_unnormalized(qrel, ranker, fname_suffix):
    print("Creating the feature file in the PWD {}".format(fname_suffix))
    with open(fname_suffix, 'w') as fw:
        qid_counter = 1
        pcount = 0
        for qid, paradict in ranker.items():
            pcount = pcount + 1
            if (pcount % 50 == 0):
                print("\n")
            else:
                print(".", end=" ")
            for pid, score in paradict.items():
                is_rel = is_relevant(qrel, qid, pid)
                qid_val = "qid:{}".format(qid_counter)
                sb = ""
                c = 1
                for score_val in score:
                    sb += str(c) + ":" + str(score_val)
                    sb += " "
                    c = c + 1
                info = "#" + qid + "_" + pid
                line = str(is_rel) + " " + qid_val + " " + sb + " " + info + "\n"
                if verbose:
                    print(line)
                fw.write(line)
            qid_counter = qid_counter + 1


def write_feature_file_normalized(df, number_of_fet, fname_suffix):
    fetlist = get_fet_col(number_of_fet)
    zcorelist = [fet + "_zscore" for fet in fetlist]
    count = 0
    rowval = None
    with open(fname_suffix, 'w') as f:
        for index, row in df.iterrows():
            binaryval = str(row['isrel']) + " "
            if row['qid'] != rowval:
                count = count + 1
            qid = "qid:" + str(count) + " "
            subcount = itertools.count(1)
            scoreslist = [str(next(subcount)) + ":" + str(row[fet]) + " " for fet in zcorelist]
            scores = ""
            for val in scoreslist:
                scores += val
            info = "#" + row['qid'] + "_" + row['pid']
            line = binaryval + qid + scores + info + '\n'
            if verbose:
                print(".")
            f.write(line)
            rowval = row['qid']


'''
Reads all the files in run files directory and put it in Dict
dict<QID,dict<PID,[0.0 0.0 0.0 ...]>
'''


def create_dictionary(runFiles):
    ranker = dict()
    number_of_feature = len(runFiles)
    current_feature_number = 0

    for run in runFiles:
        print("Working on file {}".format(run))
        with open(run, 'r') as f:
            for line in f:
                data = line.split(" ")
                qid = data[0]
                pid = data[2]
                score = data[4]

                if qid in ranker:
                    paraExtract = ranker.get(qid)
                    if pid in paraExtract:
                        list = paraExtract.get(pid)
                        list[current_feature_number] = score
                    else:
                        scorelist = [0.0 for x in range(0, number_of_feature)]
                        scorelist[current_feature_number] = score
                        paraExtract[pid] = scorelist
                else:
                    scorelist = [0.0 for x in range(0, number_of_feature)]
                    scorelist[current_feature_number] = score
                    inner = dict()
                    inner[pid] = scorelist
                    ranker[qid] = inner

        current_feature_number = current_feature_number + 1

    return ranker


'''
Helper functions to read the Qrel file into dict
'''


def readQrel(qrelpath):
    Qrel = dict()
    with open(qrelpath, 'r') as qrel:
        for line in qrel:
            data = line.split(" ")
            key = data[0]
            value = data[2]
            if Qrel.get(key) is None:
                para_list = []
                para_list.append(value)
                Qrel[key] = para_list
            else:
                Qrel.get(key).append(value)

    return Qrel


'''
Helper functions to display the list of file
'''


def dump_file_out(fileList):
    for file in fileList:
        print(file)


'''
Helper functions to display the qrel file
'''


def display_qrel_out(Qrel):
    for key, value in Qrel.items():
        for para in value:
            print(key, para)


'''
Helper functions to display the updated score file
'''


def display_dict_out(Qrel):
    for key, value in Qrel.items():
        for k, v in value.items():
            print(key, k, v)


'''
Creates te columns based on the number of features
This is used to create pandas DF
isrelevant,qid,pid,fet1,fet2 ...... fetn
'''


def get_columns(number_of_fet):
    col = []
    col.append("isrel")
    col.append("qid")
    col.append("pid")

    for i in range(0, number_of_fet):
        col.append("fet" + str(i + 1))
    return col


def get_fet_col(number_of_fet):
    fet = []
    for i in range(number_of_fet):
        fet.append("fet" + str(i + 1))
    return fet


'''
Convert dictionary into list
'''


def convert_dict_to_list(ranker, qrel):
    rowlists = []
    for qid, pval in ranker.items():
        for pid, slist in pval.items():
            temp = []
            temp.append(is_relevant(qrel, qid, pid))
            temp.append(qid)
            temp.append(pid)
            for val in slist:
                temp.append(val)
            rowlists.append(temp)
    return rowlists


def create_data_frame(rowlist, number_of_fet):
    col = get_columns(number_of_fet)
    fet_data_frame = pd.DataFrame(rowlist, columns=col)
    return fet_data_frame


'''
Read the file names in to list
'''


def getFileList(path):
    # return [os.path.join(path, file) for file in os.listdir(path) if os.path.isdir(file)]
    filelist = list()
    for file in os.listdir(path):
        absolute_path = os.path.join(path, file)
        if os.path.isdir(absolute_path):
            continue
        filelist.append(absolute_path)
    return filelist


'''
Helper function to Display the 
row list created using the dictionary
'''


def run_rank_lib(rpath, fetfile):
    if not os.path.exists(fetfile):
        print("Feature file does not exists in the current working directory")
        sys.exit(-1)

    rlib_command = 'java -jar {} -train {} -ranker 4 -metric2t MAP -save model.txt'.format(rpath, fetfile)
    process = sp.Popen(rlib_command.split(), stdout=sp.PIPE)
    for line in process.stdout:
        sys.stdout.write(line.decode('utf-8'))
    exitcode = process.wait()
    if exitcode == 0:
        print("Sub process exited gracefully")
    else:
        print("Ranklib process did not exit gracefully, exiting the program")
        sys.exit(-1)


'''
Reads te model file
'''


def get_weights(model):
    weights = list()
    with open(model, 'r') as f:
        for line in f:
            if not line.startswith('#'):
                data = line.split(" ")
                for val in data:
                    w = float(val.split(":")[1])
                    weights.append(w)
    return weights


def get_qid_pid(line):
    match2 = line.split(" ")
    qid_pid = match2[-1]
    qid_pid = qid_pid[1:]
    qid_pid = qid_pid.split("_")
    qid = qid_pid[0].rstrip()
    pid = qid_pid[1].rstrip()
    return (qid, pid)


def check_for_file(file1, file2):
    if not os.path.exists(file1):
        print("model file does not exists in the current working directory")
        sys.exit(-1)

    if not os.path.exists(file2):
        print("Feature file does not exists in the current working directory")
        sys.exit(-1)


def get_combined_run_dict(modelfile, fetfile):
    check_for_file(modelfile, fetfile)
    combined_dict = dict()
    weights = get_weights(modelfile)
    print("The weight vector is {}".format(weights))

    with open(fetfile, 'r') as f:
        for line in f:
            score = 0.0
            match1 = fetextract.findall(line)
            if match1 is not None:
                if len(match1) == len(weights):
                    for index, fet in enumerate(match1):
                        s = float(fet.split(":")[1])
                        w = weights[index]
                        score += (s * w)
            else:
                print("Some issues using regex while extracting the feature values")
                sys.exit(-1)

            qid, pid = get_qid_pid(line)
            if qid in combined_dict:
                paradict_extract = combined_dict.get(qid)
                paradict_extract[pid] = score

            else:
                paradict = dict()
                paradict[pid] = score
                combined_dict[qid] = paradict
    return combined_dict


def sort_dict(combined_dict):
    sorted_combined = dict()
    for qid, unsorted in combined_dict.items():
        sorted_data = sorted(unsorted.items(), key=operator.itemgetter(1))
        sorted_combined[qid] = sorted_data
    return sorted_combined


def create_combined_run_file(combineddict):
    with open("combined_run.txt", 'w') as f:
        for qid, pidDict in combineddict.items():
            ranking = 0
            for pid, score in pidDict.items():
                ranking = ranking + 1
                line = qid + " " + "Q0" + " " + pid + " " + str(ranking) + " " + str(
                    score) + " " + "Combined_run" + "\n"
                if verbose:
                    print(line)
                f.write(line)


def disp_row_list(rowlist):
    for val in rowlist:
        print(val)


def normalize_data_frame(frame, number_of_fet):
    fetlist = get_fet_col(number_of_fet)

    for fet in fetlist:
        np_array = np.array(frame[fet])
        z_score = zscore(np_array.astype(np.float))
        frame[fet + "_zscore"] = z_score
        if verbose:
            print(z_score)


if __name__ == '__main__':
    parser = argparse.ArgumentParser("RankLib File Formatter")
    parser.add_argument("-q", "--qrelpath", help="Path to the Qrel file", required=True)
    parser.add_argument("-d", "--dirpath", help="Path to the Qrel file", required=True)
    parser.add_argument("-v", "--verbose", help="Display information on the stdout", action="store_true")
    parser.add_argument("-s", "--suffix", help="Pass a filename suffix")
    parser.add_argument("-r", "--ranklib", help="Path to the RankLib jar")
    parser.add_argument("-n", "--normalize", help="Perform Z score normalize on the data", action="store_true")
    parser.add_argument("-m", "--modelfile", help="Pass model file, this is for the test set")

    args = parser.parse_args(args=None if sys.argv[1:] else ['--help'])

    Qrel = None
    runFiles = None

    if args.qrelpath:
        Qrel = readQrel(args.qrelpath)

    if args.dirpath:
        runFiles = getFileList(args.dirpath)

    if args.verbose:
        verbose = True
        display_qrel_out(Qrel)
        dump_file_out(runFiles)

    ranker = create_dictionary(runFiles)

    if (args.verbose):
        display_dict_out(ranker)

    fname = ""
    if args.suffix:
        fname = args.suffix + ".txt"
    else:
        fname = "featurefile.txt"

    print("Filename = " + fname)
    if args.normalize:
        rowlist = convert_dict_to_list(ranker, Qrel)
        df = create_data_frame(rowlist, len(runFiles))
        normalize_data_frame(df, len(runFiles))
        if (args.verbose):
            print(df.head())
        write_feature_file_normalized(df, len(runFiles), fname)
    else:
        write_feature_file_unnormalized(Qrel, ranker, fname)

    if args.ranklib:
        run_rank_lib(args.ranklib, fname)
        out = get_combined_run_dict("model.txt", fname)
        create_combined_run_file(out)

    if args.modelfile:
        out = get_combined_run_dict("model.txt", fname)
        create_combined_run_file(out)
