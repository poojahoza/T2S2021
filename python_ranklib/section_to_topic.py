'''
@Author: poojaoza
'''

#! /usr/bin/evn/python3

import sys
import argparse
import operator
import os


from collections import defaultdict, OrderedDict
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
			sorted_query_dict = sorted(merged_file[query_id].items(), key=lambda kv:kv[1], reverse=True)
			final_sorted_dict = OrderedDict(sorted_query_dict)
			sorted_dict_rank = 1
			for entity_id in final_sorted_dict:
				feature_value = str(final_sorted_dict[entity_id])
				output_line = query_id + " Q0 " + entity_id + " "+ str(sorted_dict_rank)+" "+ feature_value + " unh-team"+"\n"
				print(output_line)
				write_line.append(output_line)
				sorted_dict_rank += 1
		output_file.writelines(write_line)
	output_file.close()


def process_run_file(run_file):
    """Processes the trec eval format run file

    Args:
        run_file (list): the list of all the trec eval format run files
    """
    rfile_dict = dict()
    # rcounter = 0
    # for rfile in run_file:
    with open(run_file, 'r') as file:
        for line in file:
            line_split = line.strip('\n').split()
            topic_id = line_split[0].split('/')[0]
            if topic_id in rfile_dict:  # if query_id is present in the dictionary
                # if entity_id|para_id is present in the dictionary
                if line_split[2] in rfile_dict[topic_id]:
                    score = float(rfile_dict[topic_id][line_split[2]])
                    # add every feature value in array[] as value to entity
                    score = score +  float(line_split[4])
                    #score.append(float(line_split[4]))
                    rfile_dict[topic_id][line_split[2]] = float(score)
                else:
                    #score = 0.0
                    # if rcounter != 0:
                    #     for i in range(rcounter):
                    #         score.append(0.0)
                    # score.append(float(line_split[4]))
                    rfile_dict[topic_id][line_split[2]] = float(line_split[4])
            else:
                rfile_dict[topic_id] = dict()
                # score = []
                # if rcounter != 0:
                #     for i in range(rcounter):
                #         score.append(0.0)
                # score.append(float(line_split[4]))
                rfile_dict[topic_id][line_split[2]] = float(line_split[4])
        # for f in rfile_dict:
        #     for e in rfile_dict[f]:
        #         fet = rfile_dict[f][e]
        #         if len(fet) < rcounter + 1:
        #             difference = rcounter + 1 - len(fet)
        #             for d in range(difference):
        #                 fet.append(0.0)
        #             rfile_dict[f][e] = fet
        # rcounter += 1
    # print(rfile_dict)
    return rfile_dict



if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        "Please provide run file location and output file name")
    parser.add_argument('--r', help='run file location')
    parser.add_argument('--o', help='output file location')
    args = parser.parse_args()

    run_file_result = process_run_file(args.r)
    write_feature_vector_file(run_file_result, args.o)
    