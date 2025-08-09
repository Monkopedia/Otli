#ifndef __OTLI_SCOPES_H__
#define __OTLI_SCOPES_H__

#define OTLI_LET(T, R, it, result, value, code) \
    T it = value; \
    R result; \
    code

#define OTLI_LET_UNIT(T, it, value, code) \
    T it = value; \
    code

#endif // __OTLI_SCOPES_H__
