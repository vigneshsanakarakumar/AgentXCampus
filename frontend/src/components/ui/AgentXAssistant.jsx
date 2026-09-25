import React, { useState } from 'react';
import api from '../../services/api';
import Card, { CardHeader, CardBody } from './Card';
import Button from './Button';
import Badge from './Badge';
import { Bot, Send, Sparkles, CheckCircle2, AlertTriangle, ArrowRight, Loader2, ShieldCheck, Wrench, FileText, CheckSquare, Layers } from 'lucide-react';

export const AgentXAssistant = ({ initialQuery = '', onActionCompleted }) => {
  const [query, setQuery] = useState(initialQuery);
  const [messages, setMessages] = useState([
    {
      sender: 'agent',
      text: "Hello! I am your AgentX Campus Orchestrator. I coordinate specialized agents for Academics, Schedules, Knowledge Base (RAG), and Task Planning.\n\nTry asking me about your classes today, assignment deadlines, attendance regulations, or request a multi-step study plan!",
      steps: [],
    }
  ]);
  const [loading, setLoading] = useState(false);
  const [currentStep, setCurrentStep] = useState('');

  const suggestions = [
    "What classes do I have today?",
    "When is my DBMS assignment due?",
    "What is my attendance in each subject?",
    "What are the attendance regulations according to college policy?",
    "Help me prepare for my DBMS internal exam.",
    "The projector in CS-204 is not working."
  ];

  const handleSend = async (qToSend) => {
    const text = qToSend || query;
    if (!text.trim() || loading) return;

    const userMessage = { sender: 'user', text };
    setMessages((prev) => [...prev, userMessage]);
    setQuery('');
    setLoading(true);
    setCurrentStep('Orchestrator Agent: Analyzing user intent & selecting specialized agent...');

    try {
      const res = await api.post('/agent/chat', { query: text });
      const data = res.data;

      if (data.steps && data.steps.length > 0) {
        for (let i = 0; i < data.steps.length; i++) {
          setCurrentStep(data.steps[i]);
          await new Promise((r) => setTimeout(r, 120));
        }
      }

      setMessages((prev) => [
        ...prev,
        {
          sender: 'agent',
          text: data.answer,
          agentType: data.agentType,
          steps: data.steps || [],
          actionData: data.actionData,
          latencyMs: data.latencyMs,
        }
      ]);

      if (onActionCompleted) {
        onActionCompleted(data);
      }
    } catch (err) {
      setMessages((prev) => [
        ...prev,
        {
          sender: 'agent',
          text: "I was unable to complete this request right now. Please verify your connection or try rephrasing your question.",
          isError: true,
        }
      ]);
    } finally {
      setLoading(false);
      setCurrentStep('');
    }
  };

  return (
    <Card className="flex flex-col h-[620px] shadow-sm">
      <CardHeader
        title="AgentX Autonomous Assistant"
        subtitle="Central Orchestrator • Academic, Schedule, RAG & Task Agents"
        action={
          <Badge variant="primary" size="sm">
            <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 mr-1.5 animate-pulse" />
            Multi-Agent Active
          </Badge>
        }
      />

      {/* Message history */}
      <div className="flex-1 overflow-y-auto p-4 space-y-4">
        {messages.map((m, idx) => (
          <div
            key={idx}
            className={`flex flex-col ${m.sender === 'user' ? 'items-end' : 'items-start'}`}
          >
            <div
              className={`max-w-[90%] rounded-xl p-3.5 text-xs leading-relaxed ${
                m.sender === 'user'
                  ? 'bg-[var(--color-primary)] text-white rounded-br-none'
                  : 'bg-[var(--color-background)] border border-[var(--color-border)] text-[var(--color-foreground)] rounded-bl-none shadow-sm'
              }`}
            >
              {m.agentType && (
                <div className="flex items-center justify-between gap-2 mb-1.5 pb-1 border-b border-[var(--color-border)]/50">
                  <div className="flex items-center gap-1.5 text-[10px] font-bold text-[var(--color-primary)] uppercase tracking-wider">
                    <Bot className="w-3.5 h-3.5" />
                    <span>{m.agentType}</span>
                  </div>
                  {m.latencyMs && (
                    <span className="text-[9px] font-mono text-[var(--color-muted-foreground)]">
                      ⚡ {m.latencyMs}ms
                    </span>
                  )}
                </div>
              )}

              <p className="whitespace-pre-wrap">{m.text}</p>

              {/* Action Card: Grievance Incident */}
              {m.actionData && m.actionData.ticketNumber && (
                <div className="mt-3 p-2.5 rounded-lg bg-[var(--color-card)] border border-[var(--color-border)] text-[var(--color-foreground)] space-y-1">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-1.5 font-bold font-mono text-[var(--color-primary)] text-xs">
                      <Wrench className="w-3 h-3 text-amber-500" />
                      <span>Incident #{m.actionData.ticketNumber}</span>
                    </div>
                    <Badge variant="warning" size="sm">Logged</Badge>
                  </div>
                  <p className="text-[10px] text-[var(--color-muted-foreground)]">
                    Location: <strong>{m.actionData.location}</strong> • Facilities Dispatched
                  </p>
                </div>
              )}

              {/* Action Card: Task Planner */}
              {m.actionData && m.actionData.tasksCreated && (
                <div className="mt-3 p-2.5 rounded-lg bg-emerald-500/10 border border-emerald-500/30 text-emerald-800 dark:text-emerald-300 space-y-1">
                  <div className="flex items-center gap-1.5 font-bold text-xs">
                    <CheckSquare className="w-3.5 h-3.5" />
                    <span>Tasks Added to Your Dashboard:</span>
                  </div>
                  {m.actionData.tasksCreated.map((t, tIdx) => (
                    <p key={tIdx} className="text-[10px]">✓ {t}</p>
                  ))}
                </div>
              )}

              {/* Multi-Agent Execution Steps */}
              {m.steps && m.steps.length > 0 && (
                <div className="mt-2.5 pt-2 border-t border-[var(--color-border)]/60 text-[10px] text-[var(--color-muted-foreground)] space-y-0.5">
                  <div className="flex items-center gap-1 font-semibold text-[9px] uppercase tracking-wider text-[var(--color-primary)] mb-1">
                    <Layers className="w-2.5 h-2.5" />
                    <span>Multi-Agent Execution Pipeline</span>
                  </div>
                  {m.steps.map((s, sIdx) => (
                    <div key={sIdx} className="flex items-center gap-1.5 font-mono">
                      <span className="text-emerald-500">✓</span>
                      <span>{s}</span>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        ))}

        {loading && (
          <div className="flex flex-col items-start space-y-1.5 animate-pulse">
            <div className="flex items-center gap-2 px-3.5 py-2.5 rounded-xl bg-[var(--color-background)] border border-[var(--color-border)] text-xs text-[var(--color-muted-foreground)]">
              <Loader2 className="w-3.5 h-3.5 animate-spin text-[var(--color-primary)]" />
              <span>{currentStep || "Orchestrating agents..."}</span>
            </div>
          </div>
        )}
      </div>

      {/* Suggested prompts */}
      <div className="px-4 py-2 border-t border-[var(--color-border)] bg-[var(--color-background)]/30 flex items-center gap-2 overflow-x-auto no-scrollbar">
        <span className="text-[10px] text-[var(--color-muted-foreground)] uppercase tracking-wider shrink-0 font-semibold">Try:</span>
        {suggestions.map((s, idx) => (
          <button
            key={idx}
            onClick={() => handleSend(s)}
            className="text-[11px] whitespace-nowrap px-2.5 py-1 rounded-full bg-[var(--color-card)] border border-[var(--color-border)] text-[var(--color-foreground)] hover:border-[var(--color-primary)]/40 hover:bg-[var(--color-primary)]/5 transition-all"
          >
            {s}
          </button>
        ))}
      </div>

      {/* Input bar */}
      <div className="p-3 border-t border-[var(--color-border)] bg-[var(--color-card)]">
        <form
          onSubmit={(e) => {
            e.preventDefault();
            handleSend();
          }}
          className="flex items-center gap-2"
        >
          <input
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Ask about timetable, assignments, attendance, college regulations, or study plans..."
            className="flex-1 bg-[var(--color-background)] text-[var(--color-foreground)] placeholder-[var(--color-muted-foreground)] text-xs rounded-lg px-3 py-2.5 border border-[var(--color-border)] focus:outline-none focus:border-[var(--color-primary)] transition-colors"
          />
          <Button
            type="submit"
            size="sm"
            disabled={!query.trim() || loading}
            icon={Send}
          >
            Send
          </Button>
        </form>
      </div>
    </Card>
  );
};

export default AgentXAssistant;
