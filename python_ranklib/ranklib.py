'''
@Author: poojaoza
'''

#! /usr/bin/evn/python3

import sys
import argparse
import os


from collections import defaultdict
from os import listdir
from os.path import join

import numpy as np
from scipy import stats


def check_file_existence(output_file_name):
    if os.path.exists(output_file_name):
        os.remove(output_file_name)


def write_feature_vector_file(merged_file, output_file_name):
    write_line = []
    check_file_existence(output_file_name)
    with open(output_file_name, "w") as output_file:
        for query_id in merged_file:
            for entity_id in merged_file[query_id]:
                feature_value = ""
                features = merged_file[query_id][entity_id]
                for f in features:
                    feature_value += " "
                    feature_value += str(f)
                output_line = query_id + " " + entity_id + feature_value + "\n"
                print(output_line)
                write_line.append(output_line)
                # output_file.write(query_id+" "+entity_id+feature_value)
        output_file.writelines(write_line)
    output_file.close()


def write_ranklib_file(merged_file, qrel_file, output_file_name):
    query_counter = 1
    lines = []
    check_file_existence(output_file_name)
    with open(output_file_name, "w") as of:
        for m in merged_file:
            for e in merged_file[m]:
                output_line = ""
                features = merged_file[m][e]
                if m in qrel_file:
                    if e in qrel_file[m]:
                        output_line += "1"
                    else:
                        output_line += "0"
                else:
                    output_line += "0"
                output_line += " qid:" + str(query_counter)
                for i, v in enumerate(features):
                    output_line += " " + str(i + 1) + ":" + str(v)
                output_line += " #" + m + "_" + e + "\n"
                lines.append(output_line)
            query_counter += 1
        of.writelines(lines)
    of.close()


def calculate_zscore(feature_dict, number_features):
    """Calculates the zscore of every feature

    Args:
        feature_dict (Dictionary): the dictionary with every feature value
    """
    print(number_features)
    for i in range(0, number_features):
        print("calculating z score for feature number : ", i)
        zscored_feature = stats.zscore(np.asarray(list(map(lambda x: x[i], [
                                       feature_dict[q][f] for q in feature_dict for f in feature_dict[q]])))).tolist()
        counter = 0
        for qy in feature_dict:
            for ft in feature_dict[qy]:
                feature_dict[qy][ft][i] = zscored_feature[counter]
                counter += 1
    return feature_dict


def merge_run_fv_files(run_dict, feature_vector_dict, run_file_len, features_num):
    for fv in feature_vector_dict:
        if fv in run_dict:
            for ent in feature_vector_dict[fv]:
                score = feature_vector_dict[fv][ent]
                if ent in run_dict[fv]:

                    for s in run_dict[fv][ent]:
                        score.append(s)
                else:
                    for i in range(0, run_file_len):
                        score.append(0.0)
                feature_vector_dict[fv][ent] = score
        else:
            for ent in feature_vector_dict[fv]:
                score = feature_vector_dict[fv][ent]
                for i in range(0, run_file_len):
                    score.append(0.0)
                feature_vector_dict[fv][ent] = score

    for qry in run_dict:
        if qry not in feature_vector_dict:
            feature_vector_dict[qry] = dict()
            for ent in run_dict[qry]:
                score = []
                for e in range(0, features_num):
                    score.append(0.0)
                for s in run_dict[qry][ent]:
                    score.append(s)
                feature_vector_dict[qry][ent] = score
    return feature_vector_dict


def process_run_file(run_file):
    """Processes the trec eval format run file

    Args:
        run_file (list): the list of all the trec eval format run files
    """
    rfile_dict = dict()
    rcounter = 0
    for rfile in run_file:
        with open(rfile, 'r') as file:
            for line in file:
                line_split = line.strip('\n').split()
                if line_split[0] in rfile_dict:  # if query_id is present in the dictionary
                    # if entity_id|para_id is present in the dictionary
                    if line_split[2] in rfile_dict[line_split[0]]:
                        score = rfile_dict[line_split[0]][line_split[2]]
                        # add every feature value in array[] as value to entity
                        score.append(float(line_split[4]))
                        rfile_dict[line_split[0]][line_split[2]] = score
                    else:
                        score = []
                        if rcounter != 0:
                            for i in range(rcounter):
                                score.append(0.0)
                        score.append(float(line_split[4]))
                        rfile_dict[line_split[0]][line_split[2]] = score
                else:
                    rfile_dict[line_split[0]] = dict()
                    score = []
                    if rcounter != 0:
                        for i in range(rcounter):
                            score.append(0.0)
                    score.append(float(line_split[4]))
                    rfile_dict[line_split[0]][line_split[2]] = score
            for f in rfile_dict:
                for e in rfile_dict[f]:
                    fet = rfile_dict[f][e]
                    if len(fet) < rcounter + 1:
                        difference = rcounter + 1 - len(fet)
                        for d in range(difference):
                            fet.append(0.0)
                        rfile_dict[f][e] = fet
            rcounter += 1
    # print(rfile_dict)
    return rfile_dict


def process_feature_vector_file(feature_vector_file_path):
    """
    process_run_file accepts a directory path and iterates through every file present in the
    directory. The format of the run_file should be :
                            query_id para_id/entity_id f1_value fx_value
    The output dict would look as follows:
    {
        {query_id1:{entity_id1: [f1_val, f2_val, f3_val]},
            {entity_id2: [f1_val, f2_val, f3_val]}
        },
        {query_id2: {entity_id1: [f1_val, f2_val, f3_val]},
            {entity_id2: [f1_val, f2_val, f3_val]}}
    }

    @params:
    run_file_path: the directory path to the run files
    run_dict: the output dictionary
    """
    print("processing feature vectors file")
    run_dict = dict()
    counter = 0
    for file in feature_vector_file_path:
        with open(file, 'r') as run_file:
            for line in run_file:
                line_split = line.strip('\n').split()
                if len(line_split) < 3:
                    print(
                        "The correct format of run file is query_id entity_id|para_id fx_value....")
                    exit(-1)
                if line_split[0] in run_dict:  # if query_id is present in the dictionary
                    # if entity_id|para_id is present in the dictionary
                    if line_split[1] in run_dict[line_split[0]]:
                        score = run_dict[line_split[0]][line_split[1]]
                        # add every feature value in array[] as value to entity
                        for l in range(2, len(line_split)):
                            score.append(float(line_split[l]))
                        run_dict[line_split[0]][line_split[1]] = score
                    else:
                        score = []
                        if counter != 0:
                            for i in range(counter):
                                score.append(0.0)
                        for l in range(2, len(line_split)):
                            score.append(float(line_split[l]))
                        run_dict[line_split[0]][line_split[1]] = score
                else:
                    run_dict[line_split[0]] = dict()
            counter += 1
    print(run_dict)
    return run_dict


def process_qrel_file(qrel_file_path):
    print("processing qrel file")
    qrel_dict = defaultdict(list)
    with open(qrel_file_path, 'r') as qrel_file:
        for line in qrel_file:
            line_split = line.strip('\n').split()
            qrel_dict[line_split[0]].append(line_split[2])
    # print(qrel_dict)
    return qrel_dict


if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        "Please provide qrel file and the run files location")
    parser.add_argument('--q', help='qrel file location')
    # parser.add_argument(
    #     '--f', help='feature vectors file list', action="append")
    # parser.add_argument('--n', type=int, help='total number of features')
    parser.add_argument('--r', help='run file list', action="append")
    parser.add_argument(
        '-z', "--zscore", help='normalize the feature vectors with zscore', action="store_true")
    parser.add_argument(
        '--rp', help='output feature vector file path without zscore')
    parser.add_argument('--zrp', help='output feature vector file path zscore')
    parser.add_argument('--ranklibp', help='output ranklib file path zscore')
    # parser.add_argument('-s',"-section", help='')
    args = parser.parse_args()
    if len(sys.argv) == 1:
        parser.print_help(sys.stderr)
        sys.exit(1)
    print(args)
    # print(args.f)
    print(args.r)
    qrel_result = process_qrel_file(args.q)
    # feature_vec_result = process_feature_vector_file(args.f)
    # number_features = 1
    # if args.n:
    #     number_features = args.n
    # print(number_features)
    run_file_result = process_run_file(args.r)
    # merged_results = merge_run_fv_files(
    #     run_file_result, feature_vec_result, len(args.r), number_features)
    # print(merged_results)
    # if args.zscore:
    # zscore_results = calculate_zscore(merged_results, number_features+len(args.r))
    # zscore_results = calculate_zscore(merged_results, len(args.r))
    # print(len(zscore_results))
    # write_feature_vector_file(zscore_results, "/media/poojaoza/ExtraDrive1/Data/projects/cs953-team1/result/output_ranking_feature_vector_section_train_zscored_python.txt")
    # write_feature_vector_file(merged_results, "/media/poojaoza/ExtraDrive1/Data/projects/cs953-team1/result/output_ranking_feature_vector_section_train_python.txt")
    # write_ranklib_file(zscore_results, qrel_result, "/media/poojaoza/ExtraDrive1/Data/projects/cs953-team1/result/output_ranking_ranklib_section_train_python.txt")

    # write_feature_vector_file(run_file_result, "/media/poojaoza/ExtraDrive1/Data/projects/cs953-team1/result/output_ranking_feature_vector_section_train_python.txt")
    write_feature_vector_file(run_file_result, args.rp)
    zscore_results = calculate_zscore(run_file_result, len(args.r))
    print(len(zscore_results))
    # write_feature_vector_file(zscore_results, "/media/poojaoza/ExtraDrive1/Data/projects/cs953-team1/result/output_ranking_feature_vector_section_train_zscored_python.txt")
    write_feature_vector_file(zscore_results, args.zrp)

    # write_ranklib_file(zscore_results, qrel_result, "/media/poojaoza/ExtraDrive1/Data/projects/cs953-team1/result/output_ranking_ranklib_section_train_python.txt")
    write_ranklib_file(zscore_results, qrel_result, args.ranklibp)
