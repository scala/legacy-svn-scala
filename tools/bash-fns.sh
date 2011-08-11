#!/usr/bin/env bash#
#
# bash-fns.sh
# by paulp
#
# At the top of a script, put
#   . bash-fns.sh
# and init will be run automatically.
#
#
# TODO: flesh out the quoting, more collections-like methods.

# don't call init if this is being run from a shell rather
# than included in another script.
init () {
  # exit on error
  set -e
  # note script location - do it up front because there's no
  # guarantee it's an absolute path, so changing dirs will lose it.
  script_directory=$(cd $(dirname $0) ; pwd)
} \
&& [[ "$0" != "-bash" ]] && init

# first argument is directory to run command in;
# remainder of arguments are command to run.
run-in-dir () {  
  local where=$1
  shift

  # Uses subshell to avoid changing script directory
  ( cd "$where" && $* )
}

# iterates over stdin, running the given command on each line.
map-stdin () {
  local cmd="$@"
  if [[ $# -eq 0 ]]; then
    cmd="echo"
  fi

  while read line; do
    $cmd "$line"
  done
} 

# not much different from map-stdin in bash land.
foreach-stdin () {
  map-stdin "$@"
}

# iterates over stdin, echoing only those lines for which the filter is true.
filter-stdin () {
  local cond="$@"

  while read line; do
    if [[ $# -eq 0 ]] || $cond "$line"; then
      echo "$line"
    fi
  done
}

# runs the command from each immediate subdirectory of the current directory.
foreach-dir () {
  local cmd="$@"

  for dir in $(ls -1) ; do
    [[ -d "$dir" ]] && run-in-dir "$dir" $cmd
  done
}
