#ifndef __CORE__HELPER_H
#define  __CORE__HELPER_H

namespace Core {
    template <class Set>
    class IsElementOf {
    public:
        IsElementOf(const Set& set) : _set(set) { }
        bool operator()(const typename Set::value_type& element)
        {
            return std::find(_set.begin(), _set.end(), element) != _set.end();    
        }
    private:
        const Set& _set;
    };

    template <class Set>
    IsElementOf<Set> isElementOf(const Set& set) { return IsElementOf<Set>(set); }

    template <class Set>
    bool isElementOf(const typename Set::value_type& element, const Set& set) 
    {
        return IsElementOf<Set>(set)(element);
    }

    
    template <class InputSeq, class OutputSeq, class CompareSet>
    void createContainmentVector(const InputSeq& input, OutputSeq& output, const CompareSet& compare)
    {
        std::transform(input.begin(), input.end(), std::back_inserter(output), isElementOf(compare));
    }

    template <class Sequence>
    unsigned int indexOf(const typename Sequence::const_iterator& iterator, const Sequence& sequence)
    {
        return sequence.size() - (sequence.end() - iterator);
    }

    template <class Sequence>
    bool eraseFrom(const typename Sequence::value_type& element, Sequence& sequence)
    {
        typename Sequence::iterator position = std::find(sequence.begin(), sequence.end(), element);
        if (position != sequence.end()) {
            sequence.erase(position);
            return true;
        }
        return false;
    }
}

#endif
