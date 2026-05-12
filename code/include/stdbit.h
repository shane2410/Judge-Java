/* 
 * stdbit.h - C23 standard bit manipulation header (Polyfill for older compilers)
 * Provides stdc_count_ones, stdc_leading_zeros, stdc_trailing_zeros, etc.
 */
#ifndef _STDBIT_H
#define _STDBIT_H

#ifdef __cplusplus
extern "C" {
#endif

#include <stdint.h>

// Macros/Functions for bit manipulation
// Using GCC built-ins as fallback
static inline int stdc_count_ones_ui(unsigned int x) { return __builtin_popcount(x); }
static inline int stdc_count_ones_ul(unsigned long x) { return __builtin_popcountl(x); }
static inline int stdc_count_ones_ull(unsigned long long x) { return __builtin_popcountll(x); }

static inline int stdc_leading_zeros_ui(unsigned int x) { return x == 0 ? 32 : __builtin_clz(x); }
static inline int stdc_leading_zeros_ul(unsigned long x) { return x == 0 ? 32 : __builtin_clzl(x); }
static inline int stdc_leading_zeros_ull(unsigned long long x) { return x == 0 ? 64 : __builtin_clzll(x); }

static inline int stdc_trailing_zeros_ui(unsigned int x) { return x == 0 ? 32 : __builtin_ctz(x); }
static inline int stdc_trailing_zeros_ul(unsigned long x) { return x == 0 ? 32 : __builtin_ctzl(x); }
static inline int stdc_trailing_zeros_ull(unsigned long long x) { return x == 0 ? 64 : __builtin_ctzll(x); }

static inline int stdc_count_zeros_ui(unsigned int x) { return 32 - __builtin_popcount(x); }

#ifdef __cplusplus
} // extern "C"

// C++ Overloads
inline int stdc_count_ones(unsigned int x) { return __builtin_popcount(x); }
inline int stdc_count_ones(unsigned long x) { return __builtin_popcountl(x); }
inline int stdc_count_ones(unsigned long long x) { return __builtin_popcountll(x); }

inline int stdc_leading_zeros(unsigned int x) { return x == 0 ? 32 : __builtin_clz(x); }
inline int stdc_leading_zeros(unsigned long x) { return x == 0 ? 32 : __builtin_clzl(x); }
inline int stdc_leading_zeros(unsigned long long x) { return x == 0 ? 64 : __builtin_clzll(x); }

inline int stdc_trailing_zeros(unsigned int x) { return x == 0 ? 32 : __builtin_ctz(x); }
inline int stdc_trailing_zeros(unsigned long x) { return x == 0 ? 32 : __builtin_ctzl(x); }
inline int stdc_trailing_zeros(unsigned long long x) { return x == 0 ? 64 : __builtin_clzll(x); }

#else
// C Type-generic macros
#define stdc_count_ones(x) _Generic((x), \
    unsigned int: stdc_count_ones_ui, \
    unsigned long: stdc_count_ones_ul, \
    unsigned long long: stdc_count_ones_ull \
)(x)
#endif

#endif // _STDBIT_H
