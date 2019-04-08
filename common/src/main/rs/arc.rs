#pragma version(1)
#pragma rs java_package_name(com.artemchep.essence.rs)
#pragma rs_fp_relaxed

rs_allocation inImage;
int inWidth;
int inHeight;

uchar4 __attribute__ ((kernel)) arc_quart_clockwise (uchar4 in, uint32_t x, uint32_t y) {
    float radius = sqrt((float) (x * x + y * y)) + 0.0001f;
    float ratio = asin(x / radius) * M_2_PI;

    float inX = ratio * inWidth;
    float inY = radius;

    if (
        0 <= inX && inX <= inWidth - 1 &&
        0 <= inY && inY <= inHeight - 1
    ) {
        uint32_t inX_uint  = (uint32_t) inX;
        uint32_t inY_uint = (uint32_t) inY;
        const uchar4 *out = rsGetElementAt(inImage, inX_uint, inY_uint);
        return *out;
    } else {
        const uchar4 *out = rsGetElementAt(inImage, 0, 0);
        return *out;
    }
}
