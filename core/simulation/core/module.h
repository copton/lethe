#ifndef __CORE__MODULE_H
#define __CORE__MODULE_H

#include "controller.h"
#include "loopprevention.h"

#include "types.h"

#include "stream.h"
#include "controller.h"
namespace Core {
    class Stream;
    class Controller;
}

namespace Core {
    class Module {
    public:
        Module(int number, const std::string vertexName, int numberofInputStreams, int numberofOutputStreams, Controller& controller);
        virtual ~Module();

        const std::string& getVertexName() const;
        const int getVertexNumber() const;

        void reset(Ice::InputStreamPtr is);
        void newPhase(int number, int numberofPhases, const BoolVector& activeInputStreams, const BoolVector& activeOutputStreams);
        void serialize(Ice::OutputStreamPtr os);
        void deserialize(Ice::InputStreamPtr is);

        void getResult(Ice::OutputStreamPtr out);
        float getProgress();
        
        enum Caller {
            SCHEDULER,
            INPUT_STREAM,
            OUTPUT_STREAM
        };
        void call(Caller caller, unsigned int streamId=0);
        void onInputStreamClose(unsigned int streamId);
        void onOutputStreamClose(unsigned int streamId);

        bool hasReaders();

    protected:
        enum Decision {
            QUIT,
            CONTINUE
        };

        void _addInputStream(Core::Stream* stream, unsigned int streamId, unsigned int moduleId);
        void _addOutputStream(Core::Stream* stream, unsigned int streamId, unsigned int moduleId);


        virtual void lethe_reset(Ice::InputStreamPtr is) =0;
        virtual void user_newPhase(int number, int numberofPhases) =0;
        virtual Decision lethe_trigger(Caller caller, unsigned int streamId) =0;
        virtual void lethe_serialize(Ice::OutputStreamPtr os) =0;
        virtual void lethe_deserialize(Ice::InputStreamPtr is) =0;
        virtual float user_getProgress() =0;
        virtual void lethe_getResult(Ice::OutputStreamPtr os) =0;

    private:
        const int _number;
        const std::string _vertexName;
        Controller& _controller;

        struct Stream {
            Stream() { }
            Stream(Core::Stream* stream, unsigned int moduleId) : stream(stream), moduleId(moduleId) { }
            Core::Stream* stream;
            unsigned int moduleId;
        };
        typedef std::vector<Stream> StreamVector;

        StreamVector  _outputStreams;
        StreamVector _inputStreams; 

        State::Module _state;
        bool _callRunning;
    };
}

#endif
