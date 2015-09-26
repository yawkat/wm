set xdata time
set timefmt "%Y-%m-%d %H:%M:%S"
set xrange ["$min":"$max"]
set yrange [0:10]

set lmargin at screen 0.1
set rmargin at screen 1
set bmargin at screen 0.2
set tmargin at screen 0.95
set style fill solid 1
set style line 50 lt 1 lc $grid lw 1
set border ls 50
unset border
set key off
set format x "%a"
set grid ytics ls 50
set tic scale 0
set xtics "$minMidDay",86400,"$max"

plot '<cat' using 1:2 with boxes lc $external, '<cat' using 1:3 with boxes lc $internal
