#if NETSTANDARD2_0
namespace System.Runtime.CompilerServices
{
    // Lets records compile on netstandard2.0, which predates this type.
    internal static class IsExternalInit { }
}
#endif
