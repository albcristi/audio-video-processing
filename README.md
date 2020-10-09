# Audio Video Processing
Here I will upload the solutions for my laboratories from my AVP class.


# Laboratory Tasks

### Task 1

The first task is splitted in 2 main parts:

1. the first phase of the video encoder: dividing the image into blocks of 8x8 pixels
    - read the PPM image and convert each pixel value from RGB to YUV
    - form 3 matrixes: one for Y components, one for U components and one for V components
    - divide the Y matrix into blocks of 8x8 values; for each block store: the 64 values/bytes from the block, the type of block (Y) and the position of the block in the image
    - divide the U and V matrixes into blocks of 8x8 values; each block stores: 4x4=16 values/bytes from the block (i.e. perform 4:2:0 subsampling, that is for each 2x2 U/V values store only one U/V value which should be the average of those 2x2=4 values), the type of block (U or V) and the position of the block in the image
    - store the list of 8x8 Y blocks and 4x4 U and V blocks and print this list on the screen (to see it it is correct)
2. the last phase of the video decoder: composing the image from a set of 8x8 pixels blocks
    - for this part you should do the opposite of the above steps, i.e. starting from a list of 8x8 Y-values blocks and subsampled 4x4 U- and V-values blocks you should compose the final PPM image and display it on a canvas/form.
