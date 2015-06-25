#!/usr/bin/env bash

set -e

#rm -rf wallpaper
#mkdir -p wallpaper
#
#ffmpeg -i "http://i.imgur.com/ilAQEm3.gif" "wallpaper/%d.png"
#
#for f in $(ls -1 wallpaper/); do
#    echo "Transforming $f..."
#    convert wallpaper/$f \
#        -colorspace gray -contrast-stretch 90%x0% \
#        -size 1x2 gradient:'rgb(131,148,150)-rgb(0,43,54)' -fx 'v.p{0,0}*u+v.p{0,1}*(1-u)' \
#        wallpaper/$f
#done

fps=25
base_frame=36
max_frame=111

start_first_frame=67
start_last_frame=$base_frame
stop_first_frame=36
stop_last_frame=101

cd wallpaper
ln -fs $base_frame.png base.png

frame_duration=$((1000 / $fps))

i=$start_first_frame
t=0
while [[ $i != $start_last_frame ]]; do
    ln -fs $i.png start$t.png
    i=$((($i % $max_frame) + 1))
    t=$(($t + frame_duration))
done

i=$stop_first_frame
t=0
while [[ $i != $stop_last_frame ]]; do
    ln -fs $i.png stop$t.png
    i=$((($i % $max_frame) + 1))
    t=$(($t + frame_duration))
done
