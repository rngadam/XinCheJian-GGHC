#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>

#include <getopt.h>             /* getopt_long() */

#include <fcntl.h>              /* low-level i/o */
#include <unistd.h>
#include <errno.h>
#include <malloc.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <sys/time.h>
#include <sys/mman.h>
#include <sys/ioctl.h>

#include <asm/types.h>          /* for videodev2.h */

#include <linux/videodev2.h>

#include <sys/poll.h>

//#define VIDIOC_USER_CIF_OVERLAY   BASE_VIDIOC_PRIVATE
//#define VIDIOC_USER_JPEG_CAPTURE  BASE_VIDIOC_PRIVATE+1
//#define VIDIOC_USER_GET_CAPTURE_INFO BASE_VIDIOC_PRIVATE+2
#define VIDIOC_USER_CIF_OVERLAY  		_IOWR ('V', BASE_VIDIOC_PRIVATE, 	cif_SuperImpose)
#define VIDIOC_USER_JPEG_CAPTURE  		_IOWR ('V', BASE_VIDIOC_PRIVATE+1, 	int)
#define VIDIOC_USER_GET_CAPTURE_INFO  		_IOWR ('V', BASE_VIDIOC_PRIVATE+2, 	int)


#define CLEAR(x) memset (&(x), 0, sizeof (x))

typedef struct {
	//input
	unsigned int source_addr; //physical-address
	unsigned int width;
	unsigned int height;
	unsigned int q_value; //jpeg_quantisation_val q_value;
	unsigned int src_format; //EncodeInputType src_format;
	unsigned int target_addr; 
	unsigned int target_size;
	
	//output
 	unsigned int header_offset;
	unsigned int thumb_offset;
	unsigned int bitstream_offset;	
	unsigned int header_size;
	unsigned int thumb_size;
	unsigned int bitstream_size;

	unsigned int normal_op;
}TCCXXX_JPEG_ENC_DATA;

typedef struct
{
	unsigned short 			chromakey;
	
	unsigned char			mask_r;
	unsigned char			mask_g;
	unsigned char			mask_b;
	
	unsigned char			key_y;
	unsigned char			key_u;
	unsigned char			key_v;
	
}si_chromakey_info;

typedef struct
{
	unsigned short 			start_x;
	unsigned short 			start_y;
	unsigned short 			width;
	unsigned short 			height;
	
	unsigned int 			buff_offset;

	si_chromakey_info		chromakey_info;			
}cif_SuperImpose;

static void
errno_exit                      (const char *           s)
{
        fprintf (stderr, "%s error %d, %s\n",
                 s, errno, strerror (errno));

        exit (EXIT_FAILURE);
}

static int
xioctl                          (int                    fd,
                                 int                    request,
                                 void *                 arg)
{
        int r;

        do r = ioctl (fd, request, arg);
        while (-1 == r && EINTR == errno);

        return r;
}

static int open_device(char* dev_name)
{
	struct stat st; 
	int fd = -1;
	
	if (-1 == stat (dev_name, &st)) {
		fprintf (stderr, "Cannot identify '%s': %d, %s\n",
				 dev_name, errno, strerror (errno));
		exit (EXIT_FAILURE);
	}

	if (!S_ISCHR (st.st_mode)) {
		fprintf (stderr, "%s is no device\n", dev_name);
		exit (EXIT_FAILURE);
	}

	//fd = open (dev_name, O_RDWR /* required */ | O_NONBLOCK, 0);
	fd = open (dev_name, O_RDWR /* required */, 0);

	if (-1 == fd) {
	fprintf (stderr, "Cannot open '%s': %d, %s\n",
		     dev_name, errno, strerror (errno));
	exit (EXIT_FAILURE);
	}
}

static void process_image(const void* p, int len)
{
	char* filename = "test.jpg";
	fputc ('.', stdout);
	fflush (stdout);
	FILE *f = fopen(filename, "w+");
	fprintf(stdout, "wrote %d\n", fwrite(p, 1, len, f));
	fflush(f);
	fclose(f);
	fprintf(stdout, "wrpte %d to %s\n", len, filename);
}

int fd = -1;

int capture(void) 
{
	struct v4l2_buffer buf;
    struct v4l2_format fmt;
	void* buf_ptr;
	void* buf_ptr_aligned;
	void* mmap_buf;
    enum v4l2_buf_type type;	
	cif_SuperImpose cif;
	TCCXXX_JPEG_ENC_DATA jpeg_data;
	struct pollfd pf;
	int ret;
	char* dev_name = "/dev/video0";
	int jpeg_quality = 100;

	struct v4l2_requestbuffers req;
	
	fd = open_device(dev_name);
	pf.fd = fd;
	pf.events |= POLLIN | POLLPRI | POLLERR | POLLHUP;

	/*
	 * set format
	 */
	CLEAR (fmt);
	fmt.type                = V4L2_BUF_TYPE_VIDEO_CAPTURE;
	fmt.fmt.pix.width       = 640; 
	fmt.fmt.pix.height      = 480;
	fmt.fmt.pix.pixelformat = V4L2_PIX_FMT_YUYV;
	fmt.fmt.pix.field       = V4L2_FIELD_NONE;
	fmt.fmt.pix.colorspace = V4L2_COLORSPACE_JPEG;
	fmt.fmt.pix.bytesperline = fmt.fmt.pix.width*2;
	fmt.fmt.pix.sizeimage = fmt.fmt.pix.bytesperline*fmt.fmt.pix.height;
	fmt.fmt.pix.priv = 0;
	
	if (-1 == xioctl (fd, VIDIOC_S_FMT, &fmt))
		errno_exit ("VIDIOC_S_FMT");

	
	/*
	 * request buffer
	 */
	CLEAR (req);
	req.count               = 2;
	req.type                = V4L2_BUF_TYPE_VIDEO_CAPTURE;
	req.memory              = V4L2_MEMORY_MMAP;
	
	if (-1 == xioctl (fd, VIDIOC_REQBUFS, &req)) {
		if (EINVAL == errno) {
				fprintf (stderr, "%s does not support "
						 "user pointer i/o\n", dev_name);
				exit (EXIT_FAILURE);
		} else {
				errno_exit ("VIDIOC_REQBUFS");
		}
	}
	if (req.count < 2) {
			fprintf (stderr, "Insufficient buffer memory on %s\n",
					 dev_name);
			exit (EXIT_FAILURE);
	}	
	fprintf(stdout, "req.count granted %d\n", req.count);

		
	/*
	 * Query buf 
	 */
	CLEAR(buf);
	buf.type        = V4L2_BUF_TYPE_VIDEO_CAPTURE;
	buf.memory      = V4L2_MEMORY_MMAP;
	buf.index       = 0;

	if (-1 == xioctl (fd, VIDIOC_QUERYBUF, &buf))
		errno_exit ("VIDIOC_QUERYBUF");
	fprintf(stdout, "buf.length %d offset %d\n", buf.length, buf.m.offset);	
		
	mmap_buf =
			mmap (NULL /* start anywhere */,
				  buf.length,
				  PROT_READ | PROT_WRITE /* required */,
				  MAP_SHARED /* recommended */,
				  fd, 
				  buf.m.offset);	
	if(mmap_buf == MAP_FAILED) {
		errno_exit ("mmap");
	}
	/*
	 * queue buf 1
	 */
	CLEAR(buf);
	buf.type        = V4L2_BUF_TYPE_VIDEO_CAPTURE;
	buf.memory      = V4L2_MEMORY_MMAP;
	buf.index       = 0;	 
	if (-1 == xioctl (fd, VIDIOC_QBUF, &buf))
		errno_exit ("VIDIOC_QBUF");
	/*
	 * queue buf 2
	 */
	CLEAR(buf);
	buf.type        = V4L2_BUF_TYPE_VIDEO_CAPTURE;
	buf.memory      = V4L2_MEMORY_MMAP;
	buf.index       = 1;	 
	if (-1 == xioctl (fd, VIDIOC_QBUF, &buf))
		errno_exit ("VIDIOC_QBUF");	

	/*
	 * start capturing
	 */
	type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
	fprintf(stdout, "Calling VIDIOC_STREAMON %d\n", VIDIOC_STREAMON);
	if (-1 == xioctl (fd, VIDIOC_STREAMON, &type))
			errno_exit ("VIDIOC_STREAMON");		
	
    /*
     * triggers camera_core.c:camera_core_poll 
     */ 	 
	ret = poll(&pf, 1, 10000);
	if(ret == 0) {
		fprintf(stderr, "timeout on poll\n");
	} else if(ret == -1) {
		errno_exit ("poll");	
	} else {
		if(pf.revents & POLLERR) {
			errno_exit ("POLLERR");	
		} if(pf.revents & POLLIN) {
			fprintf(stdout, "data received!\n");
		}
	}
	
	/*
	 * superimpose CIF
	 */
	fprintf(stdout, "Calling ioctl VIDIOC_USER_CIF_OVERLAY %ld\n", VIDIOC_USER_CIF_OVERLAY);
	if (-1 == xioctl(fd, VIDIOC_USER_CIF_OVERLAY, &cif)) {
			errno_exit ("VIDIOC_USER_CIF_OVERLAY");			
	}
	fprintf(stdout, "start_x %d, start_y %d width %d height %d\n", cif.start_x, cif.start_y, cif.width, cif.height);
	/*
	 * capture jpeg
	 */		
	fprintf(stdout, "Calling ioctl VIDIOC_USER_JPEG_CAPTURE %ld\n", VIDIOC_USER_JPEG_CAPTURE);
	if (-1 == xioctl(fd, VIDIOC_USER_JPEG_CAPTURE, &jpeg_quality)) {
		switch (errno) {
			case EAGAIN:
					perror("Could not capture jpeg");
					return 0;

			case EIO:
					/* Could ignore EIO, see spec. */

					/* fall through */

			default:
					errno_exit ("VIDIOC_DQBUF");
		}
	}	
	
	/*
	 * retrieve buf
	 */
	CLEAR(buf);
	buf.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
	buf.memory = V4L2_MEMORY_MMAP;	
	while(-1 == xioctl (fd, VIDIOC_DQBUF, &buf)) {
		switch (errno) {
		case EAGAIN:
			perror("Could not dequeue image");		
			//return 0;

		case EIO:
			/* Could ignore EIO, see spec. */

			/* fall through */

		default:		
			perror("VIDIOC_DQBUF");
		}
	}
	
	fprintf(stdout, "bytes used: %d\n", buf.bytesused);
	/*
	 * fetch JPEG data
	 */
	CLEAR(jpeg_data);
	if (-1 == xioctl (fd, VIDIOC_USER_GET_CAPTURE_INFO, &jpeg_data))
			errno_exit ("VIDIOC_USER_GET_CAPTURE_INFO");		
	fprintf(stdout, "header offset: %d thumb offset: %d, bitstream_offset: %d header_size: %d thumb_size %d bitstream_size: %d\n", jpeg_data.header_offset, jpeg_data.thumb_offset, jpeg_data.bitstream_offset, jpeg_data.header_size, jpeg_data.thumb_size, jpeg_data.bitstream_size);
	process_image(mmap_buf+jpeg_data.header_offset, buf.bytesused);	
}

int main(void)
{

	capture();
	if(fd != -1)
		close(fd);
}