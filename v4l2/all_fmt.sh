formats="yuv420p  yuyv422     rgb24       bgr24       yuv422p     yuv444p     yuv410p     yuv411p     gray        monow      monob       pal8        yuvj420p    yuvj422p    yuvj444p    uyvy422     bgr8        bgr4_byte   rgb8        rgb4_byte   nv12        nv21        argb        rgba        abgr        bgra        gray16be    gray16le    yuv440p     yuvj440p    yuva420p    rgb48be     rgb48le     rgb565le    rgb555le    bgr565le    bgr555le    yuv420p16le yuv420p16be yuv422p16le yuv422p16be yuv444p16le yuv444p16be y400a       bgr48be     bgr48le     yuv420p9le  yuv420p10le"

for f in $formats
do
./ffmpeg -f video4linux2 -r 30 -i /dev/video0 -f rawvideo -pix_fmt $f raw.$f
done