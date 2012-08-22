#ifndef __CORE__STREAM_H
#define __CORE__STREAM_H

#include <vector>
#include <Ice/Ice.h>

#include "types.h"
#include "module.h"
#include "state.h"
namespace Core {
    class Module;
}

namespace Core {
    class Stream {
    public:
        Stream(unsigned int number, unsigned int numberofReaders, unsigned int numberofWriters);
        virtual ~Stream();

        void addWriter(Core::Module* module, unsigned int streamId, unsigned int moduleId);
        void addReader(Core::Module* module, unsigned int streamId, unsigned int moduleId);
        
        void reset();
        void newPhase(BoolVector activeWriters, BoolVector activeReaders, BoolVector emptyBuffer);
        void serialize(Ice::OutputStreamPtr& os);
        void deserialize(Ice::InputStreamPtr& is);

        void onWriterQuit(unsigned int moduleId);
        void onReaderQuit(unsigned int moduleId);

        int getEdgeNumber() const;
        bool hasReaders() const;

    protected:
        void _onRead(size_t count, unsigned int moduleId);
        bool _onWrite(size_t count, unsigned int moduleId, bool throwWhenStreamHasNoReaders);
        bool _isActive(unsigned int moduleId);

        virtual void _reset() =0;
        virtual void _newPhase(const BoolVector& activeReaders, const BoolVector& emptyBuffer) =0;
        virtual void _serialize(Ice::OutputStreamPtr& os) =0;
        virtual void _deserialize(Ice::InputStreamPtr& is) =0;

        virtual void _onReaderQuit(unsigned int moduleId) =0;
        virtual void _onWriterQuit(unsigned int moduleId) =0;
        virtual size_t _usageof(unsigned int moduleId) const =0;
        virtual size_t _sizeofFreeSpace() const =0;
        virtual int _idofModuleWithMaxUsage() const =0;

    private:
        void _updateCache();
            
    private:
        const unsigned int _number;

        struct Module {
            Module() { }
            Module(Core::Module* module, unsigned int streamId) : module(module), streamId(streamId) { }
            Core::Module* module;
            unsigned int streamId;
        };

        typedef std::vector<Module> ModuleVector;
        ModuleVector _readers;
        ModuleVector _writers;

        State::Stream _state;
        struct Cache {
            int numberofActiveReaders;
        } _cache;
    };
}

#endif
